package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.KonsistensenIGrunnlagetErFeilException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType

class TjenestepensjonSimuleringPre2025FacadeTest : ShouldSpec({

    context("pre-2025 offentlig AFP avslått") {
        should("gi resultat med 'annen klientfeil' og problembeskrivelse") {
            TjenestepensjonSimuleringPre2025Facade(
                beregningService = arrangeProblem(e = Pre2025OffentligAfpAvslaattException(message = "AFP avslått")),
                tjenestepensjonSimulator = mockk()
            ).simuler(
                simuleringSpec = simuleringSpec(),
                stillingsprosentSpec = StillingsprosentSpec(
                    stillingsprosentOffHeltUttak = null,
                    stillingsprosentOffGradertUttak = null
                )
            ) shouldBe SimulerOffentligTjenestepensjonResult(
                tpnr = "",
                navnOrdning = "",
                problem = Problem(
                    type = ProblemType.ANNEN_KLIENTFEIL,
                    beskrivelse = "AFP avslått"
                )
            )
        }
    }

    context("konsistensfeil uten beskrivelse") {
        should("gi resultat med 'annen klientfeil' og navnet på exception") {
            TjenestepensjonSimuleringPre2025Facade(
                beregningService = arrangeProblem(
                    e = KonsistensenIGrunnlagetErFeilException(e = MatchException(null, null))
                ),
                tjenestepensjonSimulator = mockk()
            ).simuler(
                simuleringSpec = simuleringSpec(),
                stillingsprosentSpec = StillingsprosentSpec(
                    stillingsprosentOffHeltUttak = null,
                    stillingsprosentOffGradertUttak = null
                )
            ) shouldBe SimulerOffentligTjenestepensjonResult(
                tpnr = "",
                navnOrdning = "",
                problem = Problem(
                    type = ProblemType.ANNEN_KLIENTFEIL,
                    beskrivelse = "java.lang.MatchException"
                )
            )
        }
    }
})

private fun arrangeProblem(e: Exception): TjenestepensjonSimuleringPre2025SpecBeregningService =
    mockk<TjenestepensjonSimuleringPre2025SpecBeregningService>().apply {
        every {
            kompletterMedAlderspensjonsberegning(any(), any())
        } throws e
    }
