package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.util.*

class Pre2025OffentligAfpPersongrunnlagTest : FunSpec({

    /**
     * En persondetalj er irrelevant hvis enten:
     * - bruk = false, eller
     * - virkTom er i fortid
     * -----------------------------------------
     * NB: Interessant forskjell mellom Pre2025OffentligAfpPersongrunnlag og EndringPersongrunnlag:
     * - Pre2025OffentligAfpPersongrunnlag bruker virkTom
     * - EndringPersongrunnlag bruker penRolleTom
     */
    test("getPersongrunnlagForSoeker should remove irrelevante persondetaljer") {
        val persongrunnlag = Pre2025OffentligAfpPersongrunnlag(
            kravService = arrangeKrav(), // med 3 relevante og 2 irrelevante persondetaljer
            persongrunnlagService = mock(PersongrunnlagService::class.java),
            epsService = mock(EpsService::class.java),
            time = { LocalDate.of(2025, 1, 1) } // "dagens dato"
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            kravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        with(persongrunnlag!!) {
            personDetaljListe.size shouldBe 3 // de 3 relevante detaljene
            personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
            personDetaljListe[1].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.FAR
            personDetaljListe[2].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.BARN
            flyktning shouldBe false // = flyktning from SimuleringSpec
            antallArUtland shouldBe 3 // = utlandAntallAar from SimuleringSpec
            sisteGyldigeOpptjeningsAr shouldBe 2023 // hardcoded
            bosattLandEnum shouldBe LandkodeEnum.NOR // hardcoded
            inngangOgEksportGrunnlag?.fortsattMedlemFT shouldBe true // hardcoded
        }
    }
})

private fun arrangeKrav(): KravService =
    mock(KravService::class.java).also {
        `when`(it.fetchKravhode(1L)).thenReturn(
            Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag()) })
    }

private fun persongrunnlag() =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                virkTom = dateAtNoon(2026, Calendar.JANUARY, 1)
                penRolleTom = dateAtNoon(1901, Calendar.JANUARY, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = false // => dvs. denne persondetaljen er irrelevant
                grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                virkTom = dateAtNoon(2026, Calendar.JANUARY, 1)
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR
                virkTom = dateAtNoon(2024, Calendar.JANUARY, 1) // => i fortid => irrelevant
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.FAR
                virkTom = null
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
                virkTom = dateAtNoon(2025, Calendar.JANUARY, 1) // => tom = "i dag" => detaljen er relevant
                penRolleTom = dateAtNoon(1901, Calendar.JANUARY, 1) // NB: penRolleTom has no effect
            }
        )
    }
