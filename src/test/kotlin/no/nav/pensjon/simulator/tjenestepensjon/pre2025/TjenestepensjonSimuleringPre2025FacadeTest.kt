package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.afp.offentlig.pre2025.TidsbegrensetOffentligAfpAvslagAarsak
import no.nav.pensjon.simulator.core.exception.KonsistensenIGrunnlagetErFeilException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType

class TjenestepensjonSimuleringPre2025FacadeTest : ShouldSpec({

    context("AFP avslått uten oppgitt årsak") {
        should("gi resultat med 'annen klientfeil' og problembeskrivelse") {
            TjenestepensjonSimuleringPre2025Facade(
                beregningService = arrangeAvslag(aarsak = null), // uten oppgitt årsak
                tjenestepensjonSimulator = mockk()
            ).simuler(simuleringSpec, stillingsprosentSpec) shouldBe
                    SimulerOffentligTjenestepensjonResult(
                        tpnr = "",
                        navnOrdning = "",
                        problem = Problem(type = ProblemType.ANNEN_KLIENTFEIL, beskrivelse = AVSLAGSBESKRIVELSE)
                    )
        }
    }

    context("AFP avslått med oppgitt årsak") {
        should("gi resultat med tilsvarende problemtype og problembeskrivelse") {
            TjenestepensjonSimuleringPre2025Facade(
                beregningService = arrangeAvslag(aarsak = TidsbegrensetOffentligAfpAvslagAarsak.FOR_LAV_ALDER),
                tjenestepensjonSimulator = mockk()
            ).simuler(simuleringSpec, stillingsprosentSpec)
                .problem shouldBe Problem(type = ProblemType.PERSON_FOR_LAV_ALDER, beskrivelse = AVSLAGSBESKRIVELSE)
        }
    }

    context("konsistensfeil uten beskrivelse") {
        should("gi resultat med 'annen klientfeil' og navnet på exception") {
            TjenestepensjonSimuleringPre2025Facade(
                beregningService = arrangeProblem(
                    e = KonsistensenIGrunnlagetErFeilException(e = MatchException(null, null))
                ),
                tjenestepensjonSimulator = mockk()
            ).simuler(simuleringSpec, stillingsprosentSpec)
                .problem shouldBe Problem(type = ProblemType.ANNEN_KLIENTFEIL, beskrivelse = "java.lang.MatchException")
        }
    }
})

private const val AVSLAGSBESKRIVELSE = "AFP avslått"

private val stillingsprosentSpec =
    StillingsprosentSpec(
        stillingsprosentOffHeltUttak = null,
        stillingsprosentOffGradertUttak = null
    )

private fun arrangeAvslag(
    aarsak: TidsbegrensetOffentligAfpAvslagAarsak?
): TjenestepensjonSimuleringPre2025SpecBeregningService =
    arrangeProblem(
        e = Pre2025OffentligAfpAvslaattException(message = AVSLAGSBESKRIVELSE, aarsak)
    )

private fun arrangeProblem(e: Exception): TjenestepensjonSimuleringPre2025SpecBeregningService =
    mockk<TjenestepensjonSimuleringPre2025SpecBeregningService>().apply {
        every {
            kompletterMedAlderspensjonsberegning(any(), any())
        } throws e
    }