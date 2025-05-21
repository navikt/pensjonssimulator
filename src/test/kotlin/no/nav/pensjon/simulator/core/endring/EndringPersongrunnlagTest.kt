package no.nav.pensjon.simulator.core.endring

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.util.*

class EndringPersongrunnlagTest : FunSpec({

    /**
     * En persondetalj er irrelevant hvis enten:
     * - bruk = false, eller
     * - penRolleTom er i fortid
     * -----------------------------------------
     * NB: Interessant forskjell mellom EndringPersongrunnlag og Pre2025OffentligAfpPersongrunnlag:
     * - EndringPersongrunnlag bruker penRolleTom
     * - Pre2025OffentligAfpPersongrunnlag bruker virkTom
     */
    test("getPersongrunnlagForSoeker should remove irrelevante persondetaljer") {
        val persongrunnlag = EndringPersongrunnlag(
            context = mock(SimulatorContext::class.java),
            kravService = arrangeKrav(), // kravet inneholder irrelevante persondetaljer
            beholdningService = mock(BeholdningerMedGrunnlagService::class.java),
            epsService = mock(EpsService::class.java),
            persongrunnlagMapper = mock(PersongrunnlagMapper::class.java),
            time = { LocalDate.of(2025, 1, 1) }
        ).getPersongrunnlagForSoeker(
            person = PenPerson(),
            spec = simuleringSpec(),
            endringKravhode = Kravhode(),
            forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
        )

        with(persongrunnlag!!) {
            personDetaljListe.size shouldBe 3
            personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
            personDetaljListe[1].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.FAR
            personDetaljListe[2].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.BARN
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
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1)
                virkTom = dateAtNoon(1901, Calendar.JANUARY, 1) // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = false // => dvs. denne persondetaljen er irrelevant
                grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1)
                virkTom = dateAtNoon(2026, Calendar.JANUARY, 1) // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR
                penRolleTom = dateAtNoon(2024, Calendar.JANUARY, 1) // => i fortid => irrelevant
                virkTom = dateAtNoon(2026, Calendar.JANUARY, 1) // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.FAR
                penRolleTom = null
                virkTom = dateAtNoon(2026, Calendar.JANUARY, 1) // NB: virkTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
                penRolleTom = dateAtNoon(2025, Calendar.JANUARY, 1) // => tom = "i dag" => detaljen er relevant
                virkTom = dateAtNoon(1901, Calendar.JANUARY, 1) // NB: virkTom has no effect
            }
        )
    }
