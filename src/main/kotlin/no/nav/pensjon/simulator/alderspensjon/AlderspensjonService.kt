package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativSimuleringService
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.TpoAlderspensjonResultMapper
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecSanitiser
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecValidator
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.RegelmotorFeilException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AlderspensjonService(
    private val simulator: SimulatorCore,
    private val alternativSimuleringService: AlternativSimuleringService
) {
    fun simulerAlderspensjon(simuleringSpec: SimuleringSpec): AlderspensjonResult {
        val spec = SimuleringSpecSanitiser.sanitise(simuleringSpec)
        SimuleringSpecValidator.validate(spec)

        val simuleringResultat = simulerMedMuligAlternativ(spec)

        return TpoAlderspensjonResultMapper.mapPensjonEllerAlternativ(
            source = simuleringResultat,
            angittFoersteUttakFom = foersteUttakFom(spec),
            angittAndreUttakFom = andreUttakFom(spec)
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
        } catch (_: UtilstrekkeligOpptjeningException) {
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
                )
        } catch (e: RegelmotorValideringException) {
            throw e //  SimuleringException("simuler alderspensjon 1963+ feilet", e)
        } catch (e: RegelmotorFeilException) {
            throw e // SimuleringException("simuler alderspensjon 1963+ feilet", e)
        }
    }

    private companion object {
        private fun foersteUttakFom(spec: SimuleringSpec): LocalDate =
            spec.foersteUttakDato ?: spec.heltUttakDato!!

        private fun andreUttakFom(spec: SimuleringSpec): LocalDate? =
            spec.foersteUttakDato?.let { spec.heltUttakDato }

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad != UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad != UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad
    }
}
