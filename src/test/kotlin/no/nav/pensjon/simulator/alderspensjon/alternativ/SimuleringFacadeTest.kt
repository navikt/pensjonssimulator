package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.ufoere.UfoereService
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
                outputConverter = mockk(),
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

    context("ufør med AFP, senere førsteuttak tillatt") {
        should("bruke normal NAU-logikk, ikke uførespesifikk") {
            val normalNauLogikk: AlternativSimuleringService = mockk(relaxed = true)
            val ufoereNauLogikk: UfoereAlternativSimuleringService = mockk()

            SimuleringFacade(
                simulator = arrangeUtilstrekkeligOpptjening(), // medfører NAU (Nært Angitt Uttak)
                alternativSimulering = normalNauLogikk,
                ufoereAlternativSimulering = ufoereNauLogikk,
                normalderService = mockk(),
                ufoereService = arrangeUfoer(), // ufør
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(
                simuleringSpec(
                    type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT, // med AFP
                    tillatSenereFoersteuttakForUfoere = true // senere førsteuttak tillatt
                ), false
            )

            verify(exactly = 1) { normalNauLogikk.simulerMedNesteLavereUttaksgrad(any(), any()) }
            verify { ufoereNauLogikk wasNot Called }
        }
    }
})

private fun arrangeUfoer(): UfoereService =
    mockk { every { hasUfoereperiode(any(), any()) } returns true }

private fun arrangeUtilstrekkeligOpptjening(): SimulatorCore =
    mockk { every { simuler(any()) } throws UtilstrekkeligOpptjeningException("10 kr") }

private fun arrangeUtilstrekkeligOpptjeningAlternativ(): AlternativSimuleringService =
    mockk {
        every {
            simulerMedNesteLavereUttaksgrad(any(), any())
        } throws UtilstrekkeligOpptjeningException("10 kr")
    }