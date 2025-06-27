package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class Pre2025OffentligAfpBeholdningTest : FunSpec({

    test("setPensjonsbeholdning should add missing beholdninger to persongrunnlag") {
        val persongrunnlag = Persongrunnlag().apply {
            fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
            beholdninger = mutableListOf(pensjonsbeholdning(aar = 2002))
        }

        Pre2025OffentligAfpBeholdning(
            context = arrangeBeholdning(
                mutableListOf(
                    // wrong type => should be ignored:
                    Pensjonsbeholdning().apply { beholdningsTypeEnum = BeholdningtypeEnum.AFP },
                    // not matching fom in persongrunnlag => missing => should be added:
                    pensjonsbeholdning(aar = 2001),
                    // matching fom in persongrunnlag => should be ignored:
                    pensjonsbeholdning(aar = 2002)
                )),
            normalderService = arrangeNormalder()
        ).setPensjonsbeholdning(
            persongrunnlag = persongrunnlag,
            forrigeAlderspensjonBeregningResult = null
        )

        with(persongrunnlag.beholdninger) {
            size shouldBe 2
            this[0].ar shouldBe 2002
            this[1].ar shouldBe 2001
        }
    }
})

private fun pensjonsbeholdning(aar: Int) =
    Pensjonsbeholdning().apply {
        beholdningsTypeEnum = BeholdningtypeEnum.PEN_B // => pensjonsbeholdning
        fom = dateAtNoon(aar, Calendar.JANUARY, 1)
        ar = aar
    }

private fun arrangeBeholdning(beholdningListe: MutableList<Pensjonsbeholdning>): SimulatorContext =
    mockk<SimulatorContext>().apply {
        every { beregnOpptjening(any(), any()) } returns beholdningListe
    }

private fun arrangeNormalder(): NormertPensjonsalderService =
    mockk<NormertPensjonsalderService>().apply {
        every { normalderOppnaasDato(LocalDate.of(1963, 1, 1)) } returns LocalDate.of(2030, 1, 1)
    }
