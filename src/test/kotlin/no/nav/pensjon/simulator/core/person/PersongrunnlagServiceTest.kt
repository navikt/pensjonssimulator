package no.nav.pensjon.simulator.core.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class PersongrunnlagServiceTest : FunSpec({

    // ===========================================
    // Tests for getPersongrunnlagForSoeker
    // ===========================================

    test("getPersongrunnlagForSoeker should return persongrunnlag mapped from person") {
        val person = PenPerson().apply {
            pid = Pid("12345678901")
            foedselsdato = LocalDate.of(1963, 1, 1)
        }
        val expectedPersongrunnlag = Persongrunnlag().apply {
            penPerson = person
        }

        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToPersongrunnlag(person, any()) } returns expectedPersongrunnlag
        }
        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns emptyBeholdningResult()
        }

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val spec = createSimuleringSpec()
        val kravhode = Kravhode()

        val result = service.getPersongrunnlagForSoeker(spec, kravhode, person)

        result shouldBe expectedPersongrunnlag
        verify { persongrunnlagMapper.mapToPersongrunnlag(person, spec) }
    }

    test("getPersongrunnlagForSoeker should add beholdninger med grunnlag to persongrunnlag") {
        val person = PenPerson().apply {
            pid = Pid("12345678901")
            foedselsdato = LocalDate.of(1963, 1, 1)
        }
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = person
        }

        val opptjeningsgrunnlag = Opptjeningsgrunnlag().apply { ar = 2020 }
        val omsorgsgrunnlag = Omsorgsgrunnlag()
        val inntektsgrunnlag = Inntektsgrunnlag()
        val dagpengegrunnlag = Dagpengegrunnlag()
        val forstegangstjeneste = Forstegangstjeneste()

        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToPersongrunnlag(person, any()) } returns persongrunnlag
        }
        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = listOf(opptjeningsgrunnlag),
                omsorgGrunnlagListe = listOf(omsorgsgrunnlag),
                inntektGrunnlagListe = listOf(inntektsgrunnlag),
                dagpengerGrunnlagListe = listOf(dagpengegrunnlag),
                forstegangstjeneste = forstegangstjeneste
            )
        }

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val spec = createSimuleringSpec()
        val kravhode = Kravhode()

        val result = service.getPersongrunnlagForSoeker(spec, kravhode, person)

        result.opptjeningsgrunnlagListe shouldHaveSize 1
        result.opptjeningsgrunnlagListe[0].ar shouldBe 2020
        result.omsorgsgrunnlagListe shouldHaveSize 1
        result.inntektsgrunnlagListe shouldHaveSize 1
        result.dagpengegrunnlagListe shouldHaveSize 1
        result.forstegangstjenestegrunnlag shouldBe forstegangstjeneste
    }

    test("getPersongrunnlagForSoeker should not add beholdninger (hentBeholdninger=false)") {
        val person = PenPerson().apply {
            pid = Pid("12345678901")
            foedselsdato = LocalDate.of(1963, 1, 1)
        }
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = person
        }

        val pensjonsbeholdning = Pensjonsbeholdning().apply { totalbelop = 1000000.0 }

        val persongrunnlagMapper = mockk<PersongrunnlagMapper> {
            every { mapToPersongrunnlag(person, any()) } returns persongrunnlag
        }
        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = listOf(pensjonsbeholdning),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        }

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val spec = createSimuleringSpec()
        val kravhode = Kravhode()

        val result = service.getPersongrunnlagForSoeker(spec, kravhode, person)

        // beholdninger should NOT be added because hentBeholdninger=false in getPersongrunnlagForSoeker
        result.beholdninger.shouldBeEmpty()
    }

    // ===========================================
    // Tests for addBeholdningerMedGrunnlagToPersongrunnlag
    // ===========================================

    test("addBeholdningerMedGrunnlagToPersongrunnlag should add all grunnlag lists") {
        val opptjeningsgrunnlag = Opptjeningsgrunnlag().apply { ar = 2021 }
        val omsorgsgrunnlag = Omsorgsgrunnlag()
        val inntektsgrunnlag = Inntektsgrunnlag()
        val dagpengegrunnlag = Dagpengegrunnlag()
        val forstegangstjeneste = Forstegangstjeneste()

        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = listOf(opptjeningsgrunnlag),
                omsorgGrunnlagListe = listOf(omsorgsgrunnlag),
                inntektGrunnlagListe = listOf(inntektsgrunnlag),
                dagpengerGrunnlagListe = listOf(dagpengegrunnlag),
                forstegangstjeneste = forstegangstjeneste
            )
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>()

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { pid = Pid("12345678901") }
        }
        val kravhode = Kravhode()

        service.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = Pid("12345678901"),
            hentBeholdninger = false
        )

        persongrunnlag.opptjeningsgrunnlagListe shouldHaveSize 1
        persongrunnlag.opptjeningsgrunnlagListe[0].ar shouldBe 2021
        persongrunnlag.omsorgsgrunnlagListe shouldHaveSize 1
        persongrunnlag.inntektsgrunnlagListe shouldHaveSize 1
        persongrunnlag.dagpengegrunnlagListe shouldHaveSize 1
        persongrunnlag.forstegangstjenestegrunnlag shouldBe forstegangstjeneste
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should add beholdninger when hentBeholdninger=true") {
        val pensjonsbeholdning = Pensjonsbeholdning().apply { totalbelop = 500000.0 }

        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = listOf(pensjonsbeholdning),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>()

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { pid = Pid("12345678901") }
        }
        val kravhode = Kravhode()

        service.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = Pid("12345678901"),
            hentBeholdninger = true
        )

        persongrunnlag.beholdninger shouldHaveSize 1
        (persongrunnlag.beholdninger[0] as Pensjonsbeholdning).totalbelop shouldBe 500000.0
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should not add beholdninger when hentBeholdninger=false") {
        val pensjonsbeholdning = Pensjonsbeholdning().apply { totalbelop = 500000.0 }

        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = listOf(pensjonsbeholdning),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>()

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { pid = Pid("12345678901") }
        }
        val kravhode = Kravhode()

        service.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = Pid("12345678901"),
            hentBeholdninger = false
        )

        persongrunnlag.beholdninger.shouldBeEmpty()
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should only add Pensjonsbeholdning types") {
        // Other Beholdning subtypes should not be added
        val pensjonsbeholdning = Pensjonsbeholdning().apply { totalbelop = 500000.0 }
        val garantipensjonsbeholdning = Garantipensjonsbeholdning()

        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = listOf(pensjonsbeholdning, garantipensjonsbeholdning),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>()

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { pid = Pid("12345678901") }
        }
        val kravhode = Kravhode()

        service.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = Pid("12345678901"),
            hentBeholdninger = true
        )

        // Only Pensjonsbeholdning should be added, not Garantipensjonsbeholdning
        persongrunnlag.beholdninger shouldHaveSize 1
        persongrunnlag.beholdninger[0] shouldBe pensjonsbeholdning
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should handle null forstegangstjeneste") {
        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>()

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { pid = Pid("12345678901") }
        }
        val kravhode = Kravhode()

        service.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = Pid("12345678901"),
            hentBeholdninger = false
        )

        persongrunnlag.forstegangstjenestegrunnlag shouldBe null
    }

    test("addBeholdningerMedGrunnlagToPersongrunnlag should replace existing grunnlag lists") {
        val existingOpptjening = Opptjeningsgrunnlag().apply { ar = 2019 }
        val newOpptjening = Opptjeningsgrunnlag().apply { ar = 2021 }

        val beholdningService = mockk<BeholdningerMedGrunnlagService> {
            every { getBeholdningerMedGrunnlag(any()) } returns BeholdningerMedGrunnlagResult(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = listOf(newOpptjening),
                omsorgGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                forstegangstjeneste = null
            )
        }
        val persongrunnlagMapper = mockk<PersongrunnlagMapper>()

        val service = PersongrunnlagService(beholdningService, persongrunnlagMapper)
        val persongrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { pid = Pid("12345678901") }
            opptjeningsgrunnlagListe = mutableListOf(existingOpptjening)
        }
        val kravhode = Kravhode()

        service.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = Pid("12345678901"),
            hentBeholdninger = false
        )

        // Should replace, not append
        persongrunnlag.opptjeningsgrunnlagListe shouldHaveSize 1
        persongrunnlag.opptjeningsgrunnlagListe[0].ar shouldBe 2021
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun emptyBeholdningResult() = BeholdningerMedGrunnlagResult(
    beholdningListe = emptyList(),
    opptjeningGrunnlagListe = emptyList(),
    omsorgGrunnlagListe = emptyList(),
    inntektGrunnlagListe = emptyList(),
    dagpengerGrunnlagListe = emptyList(),
    forstegangstjeneste = null
)

private fun createSimuleringSpec() = SimuleringSpec(
    type = SimuleringTypeEnum.ALDER,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = LocalDate.of(2029, 1, 1),
    heltUttakDato = LocalDate.of(2032, 6, 1),
    pid = Pid("12345678901"),
    foedselDato = LocalDate.of(1963, 1, 1),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_100,
    forventetInntektBeloep = 250000,
    inntektUnderGradertUttakBeloep = 125000,
    inntektEtterHeltUttakBeloep = 67500,
    inntektEtterHeltUttakAntallAar = 5,
    foedselAar = 1963,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = false,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)
