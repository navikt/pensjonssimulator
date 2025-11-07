package no.nav.pensjon.simulator.core.endring

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class EndringPersongrunnlagTest : ShouldSpec({

    /**
     * En persondetalj er irrelevant hvis enten:
     * - bruk = false, eller
     * - penRolleTom er i fortid
     * -----------------------------------------
     * NB: Interessant forskjell mellom EndringPersongrunnlag og Pre2025OffentligAfpPersongrunnlag:
     * - EndringPersongrunnlag bruker penRolleTom
     * - Pre2025OffentligAfpPersongrunnlag bruker virkTom
     */
    should("fjerne irrelevante persondetaljer") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mockk(),
            kravService = arrangeKrav(), // kravet inneholder irrelevante persondetaljer
            beholdningService = mockk(),
            epsService = mockk(),
            persongrunnlagMapper = mockk(),
            generelleDataHolder = mockk(relaxed = true),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        with(persongrunnlag!!) {
            personDetaljListe shouldHaveSize 3
            personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
            personDetaljListe[1].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.FAR
            personDetaljListe[2].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.BARN
        }
    }
})

private fun arrangeKrav(): KravService =
    mockk<KravService>().apply {
        every { fetchKravhode(1L) } returns
                Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag()) }
    }

private fun persongrunnlag() =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
                virkTom = LocalDate.of(1901, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = false // => dvs. denne persondetaljen er irrelevant
                grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                penRolleTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon()
                virkTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR
                penRolleTom = LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon() // => i fortid => irrelevant
                virkTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.FAR
                penRolleTom = null
                virkTom = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
                penRolleTom = LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon() // => tom = "i dag" => detaljen er relevant
                virkTom = LocalDate.of(1901, 1, 1).toNorwegianDateAtNoon() // NB: virkTom has no effect
            }
        )
    }
