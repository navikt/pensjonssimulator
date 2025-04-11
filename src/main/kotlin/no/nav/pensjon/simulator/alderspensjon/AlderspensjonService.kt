package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativSimuleringService
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.TpoAlderspensjonResultMapper
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecSanitiser.sanitise
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecValidator.validate
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AlderspensjonService(
    private val simulator: SimulatorCore,
    private val alternativSimuleringService: AlternativSimuleringService,
    private val vedtakService: VedtakService,
    private val generelleDataHolder: GenerelleDataHolder,
    private val time: Time
) {
    // Used for V4
    //TODO merge this with SimuleringFacade?
    fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult {
        val vedtakInfo = vedtakService.vedtakStatus(spec.pid, foersteUttakFom(spec))
        checkForGjenlevenderettighet(vedtakInfo)
        val foedselsdato = generelleDataHolder.getPerson(spec.pid).foedselDato

        val simuleringSpec = sanitise(
            AlderspensjonSpecMapper.simuleringSpec(
                source = spec,
                foedselsdato,
                erFoerstegangsuttak = vedtakInfo.harGjeldendeVedtak.not()
            )
        )

        validate(simuleringSpec, time.today())
        val simuleringResultat = simulerMedMuligAlternativ(simuleringSpec)

        return TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
            source = simuleringResultat,
            angittFoersteUttakFom = foersteUttakFom(simuleringSpec),
            angittAndreUttakFom = andreUttakFom(simuleringSpec),
            onlyIncludeEntriesForUttakDatoer = false
        )
    }

    // PEN: simdomSimulerAlderspensjon1963Plus
    private fun simulerMedMuligAlternativ(spec: SimuleringSpec): SimulertPensjonEllerAlternativ {
        try {
            val result: SimulatorOutput = simulator.simuler(spec)

            return SimulertPensjonEllerAlternativ(
                pensjon = SimulatorOutputConverter.pensjon(result), // SimulatorOutput -> SimulertPensjon
                alternativ = null
            )
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return if (isReducible(spec.uttakGrad))
                alternativSimuleringService.simulerMedNesteLavereUttaksgrad(
                    spec,
                    inkluderPensjonHvisUbetinget = true
                )
            else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec,
                    inkluderPensjonHvisUbetinget = true
                ) ?: throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return if (isReducible(spec.uttakGrad))
                alternativSimuleringService.simulerMedNesteLavereUttaksgrad(
                    spec,
                    inkluderPensjonHvisUbetinget = true
                )
            else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec,
                    inkluderPensjonHvisUbetinget = true
                ) ?: throw e
        }
    }

    private companion object {

        private fun foersteUttakFom(spec: AlderspensjonSpec): LocalDate =
            spec.gradertUttak?.fom ?: spec.heltUttakFom

        private fun foersteUttakFom(spec: SimuleringSpec): LocalDate =
            spec.foersteUttakDato ?: spec.heltUttakDato!!

        private fun andreUttakFom(spec: SimuleringSpec): LocalDate? =
            spec.foersteUttakDato?.let { spec.heltUttakDato }

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad != UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad != UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad

        // PEN: SimuleringServiceBase.checkForGjenlevenderettighet
        private fun checkForGjenlevenderettighet(vedtakInfo: VedtakStatus) {
            if (vedtakInfo.harGjenlevenderettighet) {
                throw FeilISimuleringsgrunnlagetException("Kan ikke simulere bruker med gjenlevenderettigheter")
            }
        }
    }
}
