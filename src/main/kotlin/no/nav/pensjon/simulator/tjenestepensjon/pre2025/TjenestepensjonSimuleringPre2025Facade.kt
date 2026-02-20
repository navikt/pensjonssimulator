package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.exception.KonsistensenIGrunnlagetErFeilException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.stereotype.Component

@Component
class TjenestepensjonSimuleringPre2025Facade(
    private val beregningService: TjenestepensjonSimuleringPre2025SpecBeregningService,
    private val tjenestepensjonSimulator: TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService
) {
    private val log = KotlinLogging.logger {}

    fun simuler(
        simuleringSpec: SimuleringSpec,
        stillingsprosentSpec: StillingsprosentSpec
    ): SimulerOffentligTjenestepensjonResult =
        try {
            val spec: TjenestepensjonSimuleringPre2025Spec =
                beregningService.kompletterMedAlderspensjonsberegning(simuleringSpec, stillingsprosentSpec)

            tjenestepensjonSimulator.simuler(spec)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            problem(e)
        } catch (e: KanIkkeBeregnesException) {
            problem(e)
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            problem(e)
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            problem(e)
        } catch (e: RegelmotorValideringException) {
            problem(e)
        }

    private fun problem(e: RuntimeException) =
        SimulerOffentligTjenestepensjonResult(
            tpnr = "",
            navnOrdning = "",
            problem = Problem(
                type = ProblemType.ANNEN_KLIENTFEIL,
                beskrivelse = message(e).also { log.warn { it } }
            )
        )

    private companion object {
        private fun message(e: RuntimeException): String =
            e.message ?: "Ukjent feil - ${e.javaClass.simpleName}"
    }
}
