package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.SimulatorFlags
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.exception.AvslagVilkaarsproevingForKortTrygdetidException
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.exception.BeregningstjenesteFeiletException
import no.nav.pensjon.simulator.core.exception.ForLavtTidligUttakException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import org.springframework.stereotype.Service
import java.time.LocalDate

// PEN: SimpleSimuleringService
// Vil brukes av Nav-klienter og tjenestepensjonsordninger
@Service
class SimuleringFacade(
    private val simulator: SimulatorCore,
    private val alternativSimuleringService: AlternativSimuleringService
) {
    fun simulerAlderspensjon(
        spec: SimuleringSpec,
        foedselDato: LocalDate?,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        try {
            val result: SimulatorOutput = simulator.simuler(spec, simulatorFlags(spec, ignoreAvslag = false))

            return SimulertPensjonEllerAlternativ(
                pensjon = pensjon(result),
                alternativ = null
            )
        } catch (_: ForLavtTidligUttakException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return if (isGradertAndReducible(spec))
                alternativSimuleringService.simulerMedNesteLavereUttakGrad(
                    spec,
                    foedselDato!!,
                    inkluderPensjonHvisUbetinget
                ) else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec,
                    foedselDato!!,
                    inkluderPensjonHvisUbetinget
                )
        } catch (_: AvslagVilkaarsproevingForKortTrygdetidException) {
            return if (isGradertAndReducible(spec))
                alternativSimuleringService.simulerMedNesteLavereUttakGrad(
                    spec,
                    foedselDato!!,
                    inkluderPensjonHvisUbetinget
                ) else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec,
                    foedselDato!!,
                    inkluderPensjonHvisUbetinget
                )
        } catch (e: BeregningsmotorValidereException) {
            //throw SimuleringException("simuler alderspensjon 1963+ feilet", e)
            throw RuntimeException("simuler alderspensjon 1963+ feilet", e)
        } catch (e: BeregningstjenesteFeiletException) {
            //throw SimuleringException("simuler alderspensjon 1963+ feilet", e)
            throw RuntimeException("simuler alderspensjon 1963+ feilet", e)
        }
    }

    private companion object {
        //TODO Sett ignoreAvslag = true hvis simulering alderspensjon for folketrygdbeholdning
        private fun simulatorFlags(spec: SimuleringSpec, ignoreAvslag: Boolean) =
            SimulatorFlags(
                inkluderLivsvarigOffentligAfp = spec.type === SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                ignoreAvslag
            )

        private fun isGradertAndReducible(spec: SimuleringSpec): Boolean =
            spec.isGradert() && isReducible(spec.uttakGrad)

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad !== UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad !== UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad
    }
}
