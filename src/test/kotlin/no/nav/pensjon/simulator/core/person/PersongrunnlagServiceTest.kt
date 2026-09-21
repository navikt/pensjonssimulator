package no.nav.pensjon.simulator.core.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningService
import no.nav.pensjon.simulator.opptjening.Pensjonsbeholdning
import no.nav.pensjon.simulator.opptjening.Pensjonsopptjening
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class PersongrunnlagServiceTest : FunSpec({

    // ===========================================
    // Tests for getPersongrunnlagForSoeker
    // ===========================================

    test("getPersongrunnlagForSoeker should return persongrunnlag mapped from person") {
        val person = person()
        val expectedPersongrunnlag = persongrunnlag(person)
        val persongrunnlagMapper = arrangePersongrunnlag(person, expectedPersongrunnlag)
        val opptjeningService = arrangeOpptjening(pensjonsopptjening = emptyBeholdningResult)
        val spec = simuleringSpec

        PersongrunnlagService(opptjeningService, persongrunnlagMapper)
            .getPersongrunnlagForSoeker(spec, kravhode, person) shouldBe expectedPersongrunnlag

        verify { persongrunnlagMapper.mapToPersongrunnlag(person, spec) }
    }

    test("getPersongrunnlagForSoeker should add beholdninger med grunnlag to persongrunnlag") {
        val person = person()
        val persongrunnlag = persongrunnlag(person)
        val forstegangstjeneste = Forstegangstjeneste()
        val persongrunnlagMapper = arrangePersongrunnlag(person, persongrunnlag)
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = listOf(Opptjeningsgrunnlag().apply { ar = 2020 }),
                omsorgGrunnlagListe = listOf(Omsorgsgrunnlag()),
                inntektGrunnlagListe = listOf(Inntektsgrunnlag()),
                dagpengerGrunnlagListe = listOf(Dagpengegrunnlag()),
                forstegangstjeneste = forstegangstjeneste
            )
        )

        val result = PersongrunnlagService(opptjeningService, persongrunnlagMapper)
            .getPersongrunnlagForSoeker(spec = simuleringSpec, kravhode, person)

        with(result) {
            opptjeningsgrunnlagListe shouldHaveSize 1
            opptjeningsgrunnlagListe[0].ar shouldBe 2020
            omsorgsgrunnlagListe shouldHaveSize 1
            inntektsgrunnlagListe shouldHaveSize 1
            dagpengegrunnlagListe shouldHaveSize 1
            forstegangstjenestegrunnlag shouldBe forstegangstjeneste
        }
    }

    test("getPersongrunnlagForSoeker should not add beholdninger (hentBeholdninger=false)") {
        val person = person()
        val pensjonsbeholdning = Pensjonsbeholdning(aar = 2024, totalbeloep = 1000000.0)
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = listOf(pensjonsbeholdning),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        )

        val result = PersongrunnlagService(opptjeningService, arrangePersongrunnlag(person))
            .getPersongrunnlagForSoeker(simuleringSpec, kravhode, person)

        // beholdninger should NOT be added because hentBeholdninger=false in getPersongrunnlagForSoeker
        result.beholdninger.shouldBeEmpty()
    }

    // ===========================================
    // Tests for addBeholdningerMedGrunnlagToPersongrunnlag
    // ===========================================

    test("addBeholdningerMedGrunnlagToPersongrunnlag should add all grunnlag lists") {
        val foerstegangstjeneste = Forstegangstjeneste()
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = listOf(Opptjeningsgrunnlag().apply { ar = 2021 }),
                omsorgGrunnlagListe = listOf(Omsorgsgrunnlag()),
                inntektGrunnlagListe = listOf(Inntektsgrunnlag()),
                dagpengerGrunnlagListe = listOf(Dagpengegrunnlag()),
                forstegangstjeneste = foerstegangstjeneste
            )
        )
        val persongrunnlag = persongrunnlag()

        PersongrunnlagService(opptjeningService, mockk())
            .addBeholdningerMedGrunnlagToPersongrunnlag(
                persongrunnlag,
                kravhode,
                pid = personId,
                hentBeholdninger = false
            )

        with(persongrunnlag) {
            opptjeningsgrunnlagListe shouldHaveSize 1
            opptjeningsgrunnlagListe[0].ar shouldBe 2021
            omsorgsgrunnlagListe shouldHaveSize 1
            inntektsgrunnlagListe shouldHaveSize 1
            dagpengegrunnlagListe shouldHaveSize 1
            forstegangstjenestegrunnlag shouldBe foerstegangstjeneste
        }
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should add beholdninger when hentBeholdninger=true") {
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = listOf(Pensjonsbeholdning(aar = 2024, totalbeloep = 500000.0)),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        )
        val persongrunnlag = persongrunnlag()

        PersongrunnlagService(opptjeningService, mockk())
            .addBeholdningerMedGrunnlagToPersongrunnlag(
                persongrunnlag,
                kravhode,
                pid = personId,
                hentBeholdninger = true
            )

        with(persongrunnlag) {
            beholdninger shouldHaveSize 1
            beholdninger[0].totalbelop shouldBe 500000.0
        }
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should not add beholdninger when hentBeholdninger=false") {
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = listOf(Pensjonsbeholdning(aar = 2024, totalbeloep = 500000.0)),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        )
        val persongrunnlag = persongrunnlag()

        PersongrunnlagService(opptjeningService, mockk())
            .addBeholdningerMedGrunnlagToPersongrunnlag(
                persongrunnlag,
                kravhode,
                pid = personId,
                hentBeholdninger = false
            )

        persongrunnlag.beholdninger.shouldBeEmpty()
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should handle null forstegangstjeneste") {
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        )
        val persongrunnlag = persongrunnlag()

        PersongrunnlagService(opptjeningService, mockk())
            .addBeholdningerMedGrunnlagToPersongrunnlag(
                persongrunnlag,
                kravhode,
                pid = personId,
                hentBeholdninger = false
            )

        persongrunnlag.forstegangstjenestegrunnlag shouldBe null
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should replace existing grunnlag lists") {
        val existingOpptjening = Opptjeningsgrunnlag().apply { ar = 2019 }
        val newOpptjening = Opptjeningsgrunnlag().apply { ar = 2021 }
        val opptjeningService = arrangeOpptjening(
            pensjonsopptjening = Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = listOf(newOpptjening),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        )
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = person()
            opptjeningsgrunnlagListe = mutableListOf(existingOpptjening)
        }

        PersongrunnlagService(opptjeningService, mockk())
            .addBeholdningerMedGrunnlagToPersongrunnlag(
                persongrunnlag,
                kravhode,
                pid = personId,
                hentBeholdninger = false
            )

        // Should replace, not append
        with(persongrunnlag) {
            opptjeningsgrunnlagListe shouldHaveSize 1
            opptjeningsgrunnlagListe[0].ar shouldBe 2021
        }
    }
})

private val personId = Pid("12345678901")

private val kravhode: Kravhode = Kravhode()

private val emptyBeholdningResult =
    Pensjonsopptjening(
        beholdningListe = emptyList(),
        opptjeningGrunnlagListe = emptyList(),
        omsorgGrunnlagListe = emptyList(),
        inntektGrunnlagListe = emptyList(),
        dagpengerGrunnlagListe = emptyList(),
        forstegangstjeneste = null
    )

private val simuleringSpec =
    SimuleringSpec(
        type = SimuleringTypeEnum.ALDER,
        sivilstatus = SivilstatusType.UGIF,
        epsHarPensjon = false,
        foersteUttakDato = LocalDate.of(2029, 1, 1),
        heltUttakDato = LocalDate.of(2032, 6, 1),
        pid = personId,
        foedselDato = LocalDate.of(1963, 1, 1),
        avdoed = null,
        isTpOrigSimulering = false,
        simulerForTp = false,
        uttakGrad = UttakGradKode.P_100,
        forventetInntektBeloep = 250000,
        inntektUnderGradertUttakBeloep = 125000,
        inntektEtterHeltUttakBeloep = 67500,
        inntektEtterHeltUttakAntallAar = 5,
        inntektEtterHeltUttakTom = null,
        foedselAar = 1963,
        utlandAntallAar = 0,
        utlandPeriodeListe = mutableListOf(),
        fremtidigInntektListe = mutableListOf(),
        brukFremtidigInntekt = false,
        inntektOver1GAntallAar = 0,
        flyktning = false,
        epsHarInntektOver2G = false,
        livsvarigOffentligAfp = null,
        tidsbegrensetOffentligAfp = null,
        erAnonym = false,
        ignoreAvslag = false,
        isHentPensjonsbeholdninger = true,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false
    )

private fun person(foedselsdato: LocalDate? = LocalDate.of(1963, 1, 1)) =
    PenPerson().apply {
        pid = personId
        this.foedselsdato = foedselsdato
    }

private fun persongrunnlag(person: PenPerson = person()) =
    Persongrunnlag().apply { penPerson = person }

private fun arrangePersongrunnlag(
    person: PenPerson,
    persongrunnlag: Persongrunnlag = persongrunnlag(person)
): PersongrunnlagMapper =
    mockk {
        every { mapToPersongrunnlag(person, any()) } returns persongrunnlag
    }

private fun arrangeOpptjening(pensjonsopptjening: Pensjonsopptjening): OpptjeningMedBeholdningService =
    mockk {
        every { pensjonsopptjening(any()) } returns pensjonsopptjening
        //every { pensjonsbeholdningPerAar(any()) } returns emptyMap()
    }