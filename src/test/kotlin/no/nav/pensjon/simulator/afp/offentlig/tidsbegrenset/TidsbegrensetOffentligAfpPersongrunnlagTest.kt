package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Utenlandsopphold
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class TidsbegrensetOffentligAfpPersongrunnlagTest : ShouldSpec({

    context("getPersongrunnlagForSoeker") {
        /**
         * En persondetalj er irrelevant hvis enten:
         * - bruk = false, eller
         * - virkTom er i fortid
         * -----------------------------------------
         * NB: Interessant forskjell mellom TidsbegrensetOffentligAfpPersongrunnlag og EndringPersongrunnlag:
         * - TidsbegrensetOffentligAfpPersongrunnlag bruker virkTom
         * - EndringPersongrunnlag bruker penRolleTom
         */
        should("fjerne irrelevante persondetaljer") {
            val persongrunnlag = TidsbegrensetOffentligAfpPersongrunnlag(
                kravService = arrangeKrav, // med 3 relevante og 2 irrelevante persondetaljer
                persongrunnlagService = mockk(),
                epsService = mockk(),
                generelleDataHolder = mockk { every { getSisteGyldigeOpptjeningsaar() } returns 2023 },
                time = { LocalDate.of(2025, 1, 1) } // "dagens dato"
            ).getPersongrunnlagForSoeker(
                person = PenPerson(),
                spec = simuleringSpec(),
                kravhode = Kravhode(),
                forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2025().apply { kravId = 1L }
            )

            with(persongrunnlag!!) {
                personDetaljListe shouldHaveSize 3 // de 3 relevante detaljene
                personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
                personDetaljListe[1].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.FAR
                personDetaljListe[2].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.BARN
                flyktning shouldBe false // = flyktning from SimuleringSpec
                antallArUtland shouldBe 3 // = utlandAntallAar from SimuleringSpec
                sisteGyldigeOpptjeningsAr shouldBe 2023
                bosattLandEnum shouldBe LandkodeEnum.NOR // hardcoded
                inngangOgEksportGrunnlag?.fortsattMedlemFT shouldBe true // hardcoded
            }
        }
    }

    context("utenlandsopphold") {
        should("mappe tilgjengelige felter") {
            TidsbegrensetOffentligAfpPersongrunnlag.utenlandsopphold(
                UtlandPeriode(
                    fom = LocalDate.of(2025, 1, 1),
                    tom = LocalDate.of(2026, 12, 31),
                    land = LandkodeEnum.AND,
                    arbeidet = true
                )
            ) shouldBeEqualToComparingFields Utenlandsopphold().apply {
                fomLd = LocalDate.of(2025, 1, 1)
                tomLd = LocalDate.of(2026, 12, 31)
                landEnum = LandkodeEnum.AND
                arbeidet = true
                pensjonsordning = null // ikke mappet
                bodd = false // ditto
            }
        }
    }
})

private val persongrunnlag =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                virkTom = LocalDate.of(2026, 1, 1)
                penRolleTom = LocalDate.of(1901, 1, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = false // => dvs. denne persondetaljen er irrelevant
                grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                virkTom = LocalDate.of(2026, 1, 1)
                penRolleTom = LocalDate.of(2026, 1, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR
                virkTom = LocalDate.of(2024, 1, 1) // => i fortid => irrelevant
                penRolleTom = LocalDate.of(2026, 1, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.FAR
                virkTom = null
                penRolleTom = LocalDate.of(2026, 1, 1) // NB: penRolleTom has no effect
            },
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN
                virkTom = LocalDate.of(2025, 1, 1) // => tom = "i dag" => detaljen er relevant
                penRolleTom = LocalDate.of(1901, 1, 1) // NB: penRolleTom has no effect
            }
        )
    }

private val arrangeKrav: KravService =
    mockk {
        every {
            fetchKravhode(kravhodeId = 1L)
        } returns Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) }
    }