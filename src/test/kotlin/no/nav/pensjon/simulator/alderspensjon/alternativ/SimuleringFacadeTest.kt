package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType

class SimuleringFacadeTest : ShouldSpec({

    context("utilstrekkelig opptjening ved alternativ simulering") {
        should("returnere resultat med problembeskrivelse") {
            SimuleringFacade(
                simulator = arrangeUtilstrekkeligOpptjening(),
                alternativSimulering = arrangeUtilstrekkeligOpptjeningAlternativ(),
                ufoereAlternativSimulering = mockk(),
                normalderService = mockk(),
                ufoereService = mockk(relaxed = true),
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(simuleringSpec, false) shouldBe
                    SimulertPensjonEllerAlternativ(
                        pensjon = null,
                        alternativ = null,
                        problem = Problem(
                            type = ProblemType.UTILSTREKKELIG_OPPTJENING,
                            beskrivelse = "UtilstrekkeligOpptjeningException"
                        )
                    )
        }
    }
})

private fun arrangeUtilstrekkeligOpptjening(): SimulatorCore =
    mockk { every { simuler(any()) } throws UtilstrekkeligOpptjeningException("10 kr") }

private fun arrangeUtilstrekkeligOpptjeningAlternativ(): AlternativSimuleringService =
    mockk {
        every {
            simulerMedNesteLavereUttaksgrad(any(), any())
        } throws UtilstrekkeligOpptjeningException("10 kr")
    }