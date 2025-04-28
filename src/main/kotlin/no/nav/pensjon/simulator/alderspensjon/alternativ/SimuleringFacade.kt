package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ufoere.UfoereService
import no.nav.pensjon.simulator.normalder.NormAlderService
import org.springframework.stereotype.Service

// PEN: SimpleSimuleringService
// Vil brukes av Nav-klienter og tjenestepensjonsordninger
@Service
class SimuleringFacade(
    private val simulator: SimulatorCore,
    private val alternativSimulering: AlternativSimuleringService,
    private val ufoereAlternativSimulering: UfoereAlternativSimuleringService,
    private val normAlderService: NormAlderService,
    private val ufoereService: UfoereService
) {
    fun simulerAlderspensjon(
        spec: SimuleringSpec,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        val gjelderUfoereMedAfp = spec.gjelderAfp() && hasUfoereperiode(spec)

        try {
            val result: SimulatorOutput = simulator.simuler(spec)

            return SimulertPensjonEllerAlternativ(
                pensjon =
                    if (spec.onlyVilkaarsproeving) null // irrelevant when finding uttak only
                    else pensjon(result, spec),
                alternativ = null
            )
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return alternativ(spec, gjelderUfoereMedAfp, inkluderPensjonHvisUbetinget) ?: throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            return alternativ(spec, gjelderUfoereMedAfp, inkluderPensjonHvisUbetinget) ?: throw e
        }
    }

    private fun alternativ(
        spec: SimuleringSpec,
        gjelderUfoereMedAfp: Boolean,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ? =
        if (gjelderUfoereMedAfp)
            if (spec.isGradert() && spec.heltUttakDato!!.isBefore(normAlderService.normAlderDato(spec.foedselDato!!)))
                ufoereAlternativSimulering.simulerMedNesteLavereUttaksgrad(spec, inkluderPensjonHvisUbetinget)
            else
                ufoereAlternativSimulering.simulerMedFallendeUttaksgrad(spec)
        else if (spec.onlyVilkaarsproeving.not() && isGradertAndReducible(spec))
            alternativSimulering.simulerMedNesteLavereUttaksgrad(spec, inkluderPensjonHvisUbetinget)
        else
            alternativSimulering.simulerAlternativHvisUtkanttilfelletInnvilges(spec, inkluderPensjonHvisUbetinget)


    private fun hasUfoereperiode(spec: SimuleringSpec): Boolean =
        spec.pid?.let { ufoereService.hasUfoereperiode(it, spec.foersteUttakDato!!) } == true

    private companion object {

        private fun isGradertAndReducible(spec: SimuleringSpec): Boolean =
            spec.isGradert() && isReducible(spec.uttakGrad)

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad !== UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad !== UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad
    }
}
