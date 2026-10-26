package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
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
        } catch (e: RegelmotorValideringException) {
            regelbrudd(e)
        } catch (e: TidsbegrensetOffentligAfpAvslaattException) {
            inndataProblem(e, type = e.aarsak?.problemType ?: ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: UtilstrekkeligOpptjeningException) {
            inndataProblem(e, type = ProblemType.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            inndataProblem(e, type = ProblemType.UTILSTREKKELIG_TRYGDETID)
        }

    private fun inndataProblem(e: RuntimeException, type: ProblemType) =
        SimulerOffentligTjenestepensjonResult.problemresultat(Problem(type, beskrivelse(e))).also {
            log.info(e) { feilmelding(aarsak = "problem i inndata") }
        }

    /**
     * Regelbrudd klassifiseres som serverfeil, da det kan skyldes inkonsistente data internt i Nav.
     */
    private fun regelbrudd(e: RegelmotorValideringException): SimulerOffentligTjenestepensjonResult =
        SimulerOffentligTjenestepensjonResult.problemresultat(
            Problem(type = ProblemType.ANNEN_SERVERFEIL, beskrivelse(e))
        ).also {
            val merknader = e.merknadListe.joinToString { it.asString() }
            log.warn(e) { feilmelding(aarsak = "regelbrudd - $merknader") }
        }

    private companion object {
        private fun feilmelding(aarsak: String): String =
            "TjenestepensjonSimuleringPre2025 feilet grunnet $aarsak"

        private fun beskrivelse(e: RuntimeException): String =
            e.javaClass.simpleName // ingen sensitiv info i beskrivelsen
    }
}