package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.person.Person
import no.nav.pensjon.simulator.testutil.TestObjects
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class TidsbegrensetAfpSpecCreatorTest : ShouldSpec({

    should("createSpec") {
        val result = TidsbegrensetAfpSpecCreator(
            grunnbeloepService = mockk(relaxed = true),
            personService = mockk {
                every { person(any()) } returns Person(
                    foedselsdato = null, // brukes ikke i denne sammenheng
                    sivilstand = null, // ditto
                    statsborgerskap = LandkodeEnum.ARG
                )
            },
            time = mockk()
        ).createSpec(
            uttakFom = LocalDate.of(2030, 1, 1),
            personinfo = PersonSpec(
                pid = pid,
                foedselsdato = LocalDate.of(1965, 10, 15),
                angittAfpOrdning = AFPtypeEnum.NAVO,
                flyktning = true,
                antallAarUtenlands = 0, // NB: brukes ikke i denne sammenheng
                utenlandsoppholdListe = listOf(
                    UtlandPeriode(
                        fom = LocalDate.of(1990, 1, 1),
                        tom = LocalDate.of(1995, 12, 31),
                        land = LandkodeEnum.ITA,
                        arbeidet = true
                    )
                ),
                forventetArbeidsinntekt = null,
                inntektMaanedenFoerAfp = null,
                eps = null
            ),
            opptjeningListe = emptyList()
        )

        with(result) {
            simuleringTypeEnum shouldBe SimuleringTypeEnum.AFP
            afpOrdningEnum shouldBe AFPtypeEnum.NAVO
            uttaksdatoLd shouldBe LocalDate.of(2030, 1, 1)
            result.persongrunnlagListe shouldHaveSize 1
            with(result.persongrunnlagListe.first()) {
                penPerson!! shouldBeEqualToComparingFields PenPerson(penPersonId = 1L).apply {
                    this.pid = TestObjects.pid
                }
                statsborgerskapEnum shouldBe LandkodeEnum.ARG
                antallArUtland shouldBe 6 // NB: utledet fra utenlandsoppholdListe
                utenlandsoppholdListe shouldHaveSize 1
                with(utenlandsoppholdListe.first()) {
                    fomLd shouldBe LocalDate.of(1990, 1, 1)
                    tomLd shouldBe LocalDate.of(1995, 12, 31)
                    landEnum shouldBe LandkodeEnum.ITA
                    arbeidet shouldBe true
                }
                personDetaljListe shouldHaveSize 1
                with(personDetaljListe.first()) {
                    grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
                    bruk shouldBe true
                    tillegg shouldBe false
                }
                sisteGyldigeOpptjeningsAr shouldBe 0
                opptjeningsgrunnlagListe shouldBe mutableListOf()
                inntektsgrunnlagListe shouldHaveSize 2
                with(inntektsgrunnlagListe.first()) {
                    belop shouldBe 0
                    inntektTypeEnum shouldBe InntekttypeEnum.FPI
                    fomLd shouldBe LocalDate.of(2030, 1, 1)
                    tomLd shouldBe null
                    bruk shouldBe true
                    grunnlagKildeEnum shouldBe GrunnlagkildeEnum.SIMULERING
                }
                with(inntektsgrunnlagListe[1]) {
                    belop shouldBe 0
                    inntektTypeEnum shouldBe InntekttypeEnum.IMFU
                    fomLd shouldBe LocalDate.of(2029, 12, 1)
                    tomLd shouldBe LocalDate.of(2029, 12, 31)
                    bruk shouldBe true
                    grunnlagKildeEnum shouldBe GrunnlagkildeEnum.SIMULERING
                }
                ufoereOpptjeningGrunnlag?.maksUtbetalingsgradPerArUTListe shouldBe mutableListOf()
                fodselsdatoLd shouldBe LocalDate.of(1965, 10, 15)
                flyktning shouldBe true
                skiltesDelAvAvdodesTP shouldBe -99 // hard-coded
                medlemIFolketrygdenSiste3Ar shouldBe true // ditto
                over60ArKanIkkeForsorgesSelv shouldBe false // ditto
                dodsdatoLd shouldBe null // ditto
                dodAvYrkesskade shouldBe false // ditto
            }
        }
    }
})