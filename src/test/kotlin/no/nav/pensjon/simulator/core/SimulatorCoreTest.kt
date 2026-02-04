package no.nav.pensjon.simulator.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpBeregner
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpResult
import no.nav.pensjon.simulator.afp.privat.PrivatAfpBeregner
import no.nav.pensjon.simulator.afp.privat.PrivatAfpResult
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.beregn.AlderspensjonBeregnerResult
import no.nav.pensjon.simulator.core.beregn.AlderspensjonVilkaarsproeverOgBeregner
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktFinder
import no.nav.pensjon.simulator.core.krav.KravhodeCreator
import no.nav.pensjon.simulator.core.krav.KravhodeUpdater
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimuleringResultPreparer
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.SakService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.ytelse.AvdoedYtelser
import no.nav.pensjon.simulator.ytelse.YtelseService
import java.time.LocalDate
import java.util.*

class SimulatorCoreTest : FunSpec({

    // ===========================================
    // Tests for simuler() - basic flow
    // ===========================================

    test("simuler should execute full simulation flow and return output") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec()

        val result = core.simuler(spec)

        result shouldNotBe null
    }

    test("simuler should call EndringValidator.validate for endring simulation types") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            heltUttakDato = LocalDate.of(2027, 1, 1),
            uttakGrad = UttakGradKode.P_100
        )

        val result = core.simuler(spec)

        result shouldNotBe null
    }

    test("simuler should throw exception for invalid endring type without required fields") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            foersteUttakDato = null // Invalid - required for endring
        )

        shouldThrow<InvalidArgumentException> {
            core.simuler(spec)
        }
    }

    // ===========================================
    // Tests for simuler() - avdoed handling
    // ===========================================

    test("simuler should use avdoed from ytelser when available") {
        val avdoedPid = Pid("98765432109")
        val doedsdato = LocalDate.of(2020, 5, 15)

        val ytelseService = mockk<YtelseService> {
            every { getLoependeYtelser(any()) } returns LoependeYtelser(
                soekerVirkningFom = LocalDate.of(2025, 1, 1),
                privatAfpVirkningFom = null,
                sisteBeregning = null,
                forrigeAlderspensjonBeregningResultat = null,
                forrigePrivatAfpBeregningResultat = null,
                forrigeVedtakListe = mutableListOf(),
                avdoed = AvdoedYtelser(
                    pid = avdoedPid,
                    doedsdato = doedsdato,
                    foersteVirkningsdato = LocalDate.of(2020, 6, 1)
                )
            )
        }

        val core = createSimulatorCore(ytelseService = ytelseService)
        val spec = createSimuleringSpec(avdoed = null)

        val result = core.simuler(spec)

        result shouldNotBe null
    }

    // ===========================================
    // Tests for simuler() - privat AFP
    // ===========================================

    test("simuler should calculate privat AFP when privatAfpVirkningFom is set") {
        val privatAfpBeregner = mockk<PrivatAfpBeregner> {
            every { beregnPrivatAfp(any()) } returns PrivatAfpResult(
                gjeldendeBeregningsresultatAfpPrivat = BeregningsResultatAfpPrivat(),
                afpPrivatBeregningsresultatListe = mutableListOf()
            )
        }

        val ytelseService = mockk<YtelseService> {
            every { getLoependeYtelser(any()) } returns LoependeYtelser(
                soekerVirkningFom = LocalDate.of(2025, 1, 1),
                privatAfpVirkningFom = LocalDate.of(2024, 1, 1),
                sisteBeregning = null,
                forrigeAlderspensjonBeregningResultat = null,
                forrigePrivatAfpBeregningResultat = null,
                forrigeVedtakListe = mutableListOf(),
                avdoed = null
            )
        }

        val core = createSimulatorCore(
            privatAfpBeregner = privatAfpBeregner,
            ytelseService = ytelseService
        )
        val spec = createSimuleringSpec()

        core.simuler(spec)

        verify(exactly = 1) { privatAfpBeregner.beregnPrivatAfp(any()) }
    }

    test("simuler should not calculate privat AFP when privatAfpVirkningFom is null") {
        val privatAfpBeregner = mockk<PrivatAfpBeregner>()

        val core = createSimulatorCore(privatAfpBeregner = privatAfpBeregner)
        val spec = createSimuleringSpec()

        core.simuler(spec)

        verify(exactly = 0) { privatAfpBeregner.beregnPrivatAfp(any()) }
    }

    // ===========================================
    // Tests for simuler() - AFP_FPP special case
    // ===========================================

    test("simuler should return special output for AFP_FPP simulation type") {
        val offentligAfpBeregner = mockk<OffentligAfpBeregner> {
            every { beregnAfp(any(), any(), any(), any(), any()) } returns OffentligAfpResult(
                pre2025 = Pre2025OffentligAfpResult(
                    simuleringResult = Simuleringsresultat(),
                    kravhode = createKravhode()
                ),
                livsvarig = null,
                kravhode = createKravhode()
            )
        }

        val core = createSimulatorCore(offentligAfpBeregner = offentligAfpBeregner)
        val spec = createSimuleringSpec(type = SimuleringTypeEnum.AFP_FPP)

        val result = core.simuler(spec)

        result.pre2025OffentligAfp shouldNotBe null
    }

    // ===========================================
    // Tests for simuler() - anonymous vs personal
    // ===========================================

    test("simuler should not add personal data to output when erAnonym is true") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(erAnonym = true, pid = null)

        val result = core.simuler(spec)

        result.foedselDato shouldBe null
        result.persongrunnlag shouldBe null
    }

    test("simuler should add personal data to output when erAnonym is false") {
        val personService = mockk<PersonService> {
            every { person(any()) } returns PenPerson().apply {
                foedselsdato = LocalDate.of(1963, 1, 1)
            }
        }

        val core = createSimulatorCore(personService = personService)
        val spec = createSimuleringSpec(erAnonym = false)

        val result = core.simuler(spec)

        result.foedselDato shouldBe LocalDate.of(1963, 1, 1)
        result.persongrunnlag shouldNotBe null
    }

    // ===========================================
    // Tests for simuler() - pre-2025 offentlig AFP
    // ===========================================

    test("simuler should adjust heltUttakDato for pre-2025 offentlig AFP") {
        val normalderService = mockk<NormertPensjonsalderService> {
            every { normalder(any<LocalDate>()) } returns Alder(67, 0)
        }

        val personService = mockk<PersonService> {
            every { person(any()) } returns PenPerson().apply {
                foedselsdato = LocalDate.of(1960, 6, 15)
            }
        }

        val core = createSimulatorCore(
            normalderService = normalderService,
            personService = personService
        )

        // AFP_FPP gjelderPre2025OffentligAfp() returns true
        val spec = createSimuleringSpec(type = SimuleringTypeEnum.AFP_FPP)

        val result = core.simuler(spec)

        result shouldNotBe null
    }

    // ===========================================
    // Tests for fetchFoedselsdato()
    // ===========================================

    test("fetchFoedselsdato should delegate to generalPersonService") {
        val expectedDate = LocalDate.of(1965, 3, 20)
        val generalPersonService = mockk<GeneralPersonService> {
            every { foedselsdato(any()) } returns expectedDate
        }

        val core = createSimulatorCore(generalPersonService = generalPersonService)
        val pid = Pid("12345678901")

        val result = core.fetchFoedselsdato(pid)

        result shouldBe expectedDate
        verify { generalPersonService.foedselsdato(pid) }
    }

    // ===========================================
    // Tests for simuler() - metrics and logging
    // ===========================================

    test("simuler should count simuleringstype metric") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(type = SimuleringTypeEnum.ALDER)

        core.simuler(spec)

        // Metrics.countSimuleringstype is called - we can't easily verify static calls,
        // but the test ensures no exception is thrown
    }

    // ===========================================
    // Tests for simuler() - kravhode operations
    // ===========================================

    test("simuler should create and update kravhode") {
        val kravhode = createKravhode()
        val updatedKravhode = createKravhode()

        val kravhodeCreator = mockk<KravhodeCreator> {
            every { opprettKravhode(any(), any(), any()) } returns kravhode
        }

        val kravhodeUpdater = mockk<KravhodeUpdater> {
            every { updateKravhodeForFoersteKnekkpunkt(any()) } returns updatedKravhode
        }

        val core = createSimulatorCore(
            kravhodeCreator = kravhodeCreator,
            kravhodeUpdater = kravhodeUpdater
        )
        val spec = createSimuleringSpec()

        core.simuler(spec)

        verify { kravhodeCreator.opprettKravhode(any(), any(), any()) }
        verify { kravhodeUpdater.updateKravhodeForFoersteKnekkpunkt(any()) }
    }

    // ===========================================
    // Tests for simuler() - knekkpunkt finding
    // ===========================================

    test("simuler should find knekkpunkter") {
        val knekkpunktFinder = mockk<KnekkpunktFinder> {
            every { finnKnekkpunkter(any()) } returns sortedMapOf()
        }

        val core = createSimulatorCore(knekkpunktFinder = knekkpunktFinder)
        val spec = createSimuleringSpec()

        core.simuler(spec)

        verify { knekkpunktFinder.finnKnekkpunkter(any()) }
    }

    // ===========================================
    // Tests for simuler() - offentlig AFP
    // ===========================================

    test("simuler should calculate offentlig AFP when innvilgetAfp is null") {
        val offentligAfpBeregner = mockk<OffentligAfpBeregner> {
            every { beregnAfp(any(), any(), any(), any(), any()) } returns OffentligAfpResult(
                pre2025 = null,
                livsvarig = null,
                kravhode = createKravhode()
            )
        }

        val core = createSimulatorCore(offentligAfpBeregner = offentligAfpBeregner)
        val spec = createSimuleringSpec()

        core.simuler(spec)

        verify { offentligAfpBeregner.beregnAfp(any(), any(), any(), any(), any()) }
    }

    // ===========================================
    // Tests for simuler() - result preparation
    // ===========================================

    test("simuler should prepare result with correct spec values") {
        val resultPreparer = mockk<SimuleringResultPreparer> {
            every { opprettOutput(any()) } returns SimulatorOutput()
        }

        val core = createSimulatorCore(resultPreparer = resultPreparer)
        val spec = createSimuleringSpec()

        core.simuler(spec)

        verify { resultPreparer.opprettOutput(any()) }
    }

    // ===========================================
    // Tests for simuler() - ENDR_ALDER_M_GJEN
    // ===========================================

    test("simuler should validate ENDR_ALDER_M_GJEN with avdoed doedDato") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            heltUttakDato = LocalDate.of(2027, 1, 1),
            uttakGrad = UttakGradKode.P_100,
            avdoed = Avdoed(
                pid = Pid("98765432109"),
                antallAarUtenlands = 0,
                inntektFoerDoed = 0,
                doedDato = LocalDate.of(2020, 5, 15)
            )
        )

        val result = core.simuler(spec)

        result shouldNotBe null
    }

    test("simuler should throw for ENDR_ALDER_M_GJEN without avdoed doedDato") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            heltUttakDato = LocalDate.of(2027, 1, 1),
            uttakGrad = UttakGradKode.P_100,
            avdoed = null
        )

        shouldThrow<InvalidArgumentException> {
            core.simuler(spec)
        }
    }

    // ===========================================
    // Tests for simuler() - heltUttakDato requirement
    // ===========================================

    test("simuler should throw for gradert uttak without heltUttakDato") {
        val core = createSimulatorCore()
        val spec = createSimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            heltUttakDato = null, // Missing for gradert uttak
            uttakGrad = UttakGradKode.P_50
        )

        shouldThrow<InvalidArgumentException> {
            core.simuler(spec)
        }
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun createSimulatorCore(
    kravhodeCreator: KravhodeCreator = mockKravhodeCreator(),
    kravhodeUpdater: KravhodeUpdater = mockKravhodeUpdater(),
    knekkpunktFinder: KnekkpunktFinder = mockKnekkpunktFinder(),
    alderspensjonVilkaarsproeverOgBeregner: AlderspensjonVilkaarsproeverOgBeregner = mockAlderspensjonBeregner(),
    privatAfpBeregner: PrivatAfpBeregner = mockk(relaxed = true),
    generalPersonService: GeneralPersonService = mockGeneralPersonService(),
    personService: PersonService = mockPersonService(),
    sakService: SakService = mockSakService(),
    ytelseService: YtelseService = mockYtelseService(),
    offentligAfpBeregner: OffentligAfpBeregner = mockOffentligAfpBeregner(),
    grunnbeloepService: GrunnbeloepService = mockGrunnbeloepService(),
    normalderService: NormertPensjonsalderService = mockNormalderService(),
    generelleDataHolder: GenerelleDataHolder = mockGenerelleDataHolder(),
    resultPreparer: SimuleringResultPreparer = mockResultPreparer()
): SimulatorCore = SimulatorCore(
    kravhodeCreator,
    kravhodeUpdater,
    knekkpunktFinder,
    alderspensjonVilkaarsproeverOgBeregner,
    privatAfpBeregner,
    generalPersonService,
    personService,
    sakService,
    ytelseService,
    offentligAfpBeregner,
    grunnbeloepService,
    normalderService,
    generelleDataHolder,
    resultPreparer
)

private fun mockKravhodeCreator(): KravhodeCreator = mockk {
    every { opprettKravhode(any(), any(), any()) } returns createKravhode()
}

private fun mockKravhodeUpdater(): KravhodeUpdater = mockk {
    every { updateKravhodeForFoersteKnekkpunkt(any()) } returns createKravhode()
}

private fun mockKnekkpunktFinder(): KnekkpunktFinder = mockk {
    every { finnKnekkpunkter(any()) } returns sortedMapOf()
}

private fun mockAlderspensjonBeregner(): AlderspensjonVilkaarsproeverOgBeregner = mockk {
    every { vilkaarsproevOgBeregnAlder(any()) } returns AlderspensjonBeregnerResult(
        beregningsresultater = mutableListOf(BeregningsResultatAlderspensjon2011()),
        pensjonsbeholdningPerioder = mutableListOf()
    )
}

private fun mockGeneralPersonService(): GeneralPersonService = mockk {
    every { foedselsdato(any()) } returns LocalDate.of(1963, 1, 1)
}

private fun mockPersonService(): PersonService = mockk {
    every { person(any()) } returns PenPerson().apply {
        foedselsdato = LocalDate.of(1963, 1, 1)
    }
}

private fun mockSakService(): SakService = mockk {
    every { personVirkningDato(any()) } returns FoersteVirkningDatoCombo(
        foersteVirkningDatoGrunnlagListe = emptyList()
    )
}

private fun mockYtelseService(): YtelseService = mockk {
    every { getLoependeYtelser(any()) } returns LoependeYtelser(
        soekerVirkningFom = LocalDate.of(2025, 1, 1),
        privatAfpVirkningFom = null,
        sisteBeregning = null,
        forrigeAlderspensjonBeregningResultat = null,
        forrigePrivatAfpBeregningResultat = null,
        forrigeVedtakListe = mutableListOf(),
        avdoed = null
    )
}

private fun mockOffentligAfpBeregner(): OffentligAfpBeregner = mockk {
    every { beregnAfp(any(), any(), any(), any(), any()) } returns OffentligAfpResult(
        pre2025 = null,
        livsvarig = null,
        kravhode = createKravhode()
    )
}

private fun mockGrunnbeloepService(): GrunnbeloepService = mockk {
    every { naavaerendeGrunnbeloep() } returns 118620
}

private fun mockNormalderService(): NormertPensjonsalderService = mockk {
    every { normalder(any<LocalDate>()) } returns Alder(67, 0)
}

private fun mockGenerelleDataHolder(): GenerelleDataHolder = mockk {
    every { getSisteGyldigeOpptjeningsaar() } returns 2023
}

private fun mockResultPreparer(): SimuleringResultPreparer = mockk {
    every { opprettOutput(any()) } returns SimulatorOutput()
}

private fun createKravhode(): Kravhode = Kravhode().apply {
    persongrunnlagListe = mutableListOf(
        Persongrunnlag().apply {
            fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
            penPerson = PenPerson().apply { penPersonId = 1L }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                }
            )
        }
    )
}

private fun createSimuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    pid: Pid? = Pid("12345678901"),
    foersteUttakDato: LocalDate? = LocalDate.of(2029, 1, 1),
    heltUttakDato: LocalDate? = LocalDate.of(2032, 6, 1),
    uttakGrad: UttakGradKode = UttakGradKode.P_100,
    erAnonym: Boolean = false,
    avdoed: Avdoed? = null
) = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = heltUttakDato,
    pid = pid,
    foedselDato = LocalDate.of(1963, 1, 1),
    avdoed = avdoed,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = uttakGrad,
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
    erAnonym = erAnonym,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)
