package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.fpp.FppSimuleringResult
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import java.time.LocalDate

class TidsbegrensetAfpSimuleringFacadeTest : ShouldSpec({

    context("simulerAfpForFpp") {
        context("feil i spesifikasjonen") {
            should("gi resultat med problem av type 'annen klientfeil'") {
                TidsbegrensetAfpSimuleringFacade(
                    specCreator = mockk(relaxed = true),
                    simulator = arrrangeBadSpec()
                ).simulerAfpForFpp(
                    TidsbegrensetAfpSpec(
                        uttakFom = LocalDate.of(2021, 1, 1),
                        personopplysninger = PersonSpec(pid = pid, utenlandsoppholdListe = emptyList()),
                        opptjeningListe = emptyList()
                    )
                ) shouldBe FppSimuleringResult(
                    afpOrdning = null,
                    beregnetAfp = null,
                    problem = Problem(type = ProblemType.ANNEN_KLIENTFEIL, beskrivelse = "BadSpecException")
                )
            }
        }
    }
})

private fun arrrangeBadSpec(): TidsbegrensetAfpSimuleringService =
    mockk { every { simulerPensjonsberegning(any()) } throws BadSpecException("feil") }