package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import org.springframework.stereotype.Service

// PEN: SimpleSimuleringService
// Vil brukes av Nav-klienter og tjenestepensjonsordninger
@Service
class SimuleringFacade(
    private val simulator: SimulatorCore,
    private val alternativSimuleringService: AlternativSimuleringService
) {
    fun simulerAlderspensjon(
        spec: SimuleringSpec,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        try {
            val result: SimulatorOutput = simulator.simuler(spec)

            return SimulertPensjonEllerAlternativ(
                pensjon = pensjon(result),
                alternativ = null
            )
        } catch (_: UtilstrekkeligOpptjeningException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return if (isGradertAndReducible(spec))
                alternativSimuleringService.simulerMedNesteLavereUttaksgrad(
                    spec,
                    inkluderPensjonHvisUbetinget
                ) else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec,
                    inkluderPensjonHvisUbetinget
                )
        } catch (_: UtilstrekkeligTrygdetidException) {
            return if (isGradertAndReducible(spec))
                alternativSimuleringService.simulerMedNesteLavereUttaksgrad(
                    spec,
                    inkluderPensjonHvisUbetinget
                )
            else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec,
                    inkluderPensjonHvisUbetinget
                )
        }
    }

    private companion object {

        private fun isGradertAndReducible(spec: SimuleringSpec): Boolean =
            spec.isGradert() && isReducible(spec.uttakGrad)

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad !== UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad !== UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad
    }
}
