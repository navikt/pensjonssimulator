package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate
import java.util.*

class KravhodeUpdaterTest : FunSpec({

    /**
     * Oppdatert karvhode skal ha:
     * - persongrunnlag med trygdeavtale der kravDatoIAvtaleland = dagens dato
     */
    test("updateKravhodeForFoersteKnekkpunkt should oppdatere kravhodet") {
        val kravhode = KravhodeUpdater(
            context = mockk(relaxed = true),
            normalderService = arrangeNormertPensjonsalder(),
            pre2025OffentligAfpBeholdning = mockk(),
            trygdetidSetter = mockk(),
            time = { LocalDate.of(2025, 1, 1) } // "dagens dato"
        ).updateKravhodeForFoersteKnekkpunkt(
            spec = KravhodeUpdateSpec(
                kravhode = Kravhode().apply {
                    persongrunnlagListe = mutableListOf(persongrunnlag())
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
                },
                simulering = simuleringSpec(),
                forrigeAlderspensjonBeregningResult = null
            )
        )

        with(kravhode) {
            persongrunnlagListe.size shouldBe 1

            persongrunnlagListe[0]
                .trygdeavtale!!.kravDatoIAvtaleland shouldBe dateAtNoon(2025, Calendar.JANUARY, 1) // = dagens dato
        }
    }
})

private fun arrangeNormertPensjonsalder(): NormertPensjonsalderService =
    mockk<NormertPensjonsalderService>().apply {
        every { normalder(foedselsdato = LocalDate.of(1963, 1, 1)) } returns Alder(62, 0)
    }

private fun persongrunnlag() =
    Persongrunnlag().apply {
        penPerson = PenPerson()
        fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1)
            }
        )
    }
