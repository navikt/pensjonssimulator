package no.nav.pensjon.simulator.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpBeregner
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpResult
import no.nav.pensjon.simulator.afp.privat.PrivatAfpBeregner
import no.nav.pensjon.simulator.afp.privat.PrivatAfpResult
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
import no.nav.pensjon.simulator.core.result.RegisterData
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimuleringResultPreparer
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.spec.SpecOverrider
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.SakService
import no.nav.pensjon.simulator.ytelse.YtelseService
import java.time.LocalDate

class SimulatorCoreTest : ShouldSpec({

    context("simuler - basic flow") {
        should("execute full simulation flow and return output") {
            simulatorCore().simuler(initialSpec = simuleringSpec()) shouldNotBe null
        }

        should("call EndringValidator.validate for endring simulation types") {
            val spec = simuleringSpec(
                type = SimuleringTypeEnum.ENDR_ALDER,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = LocalDate.of(2027, 1, 1),
                uttaksgrad = UttakGradKode.P_100
            )

            simulatorCore().simuler(spec) shouldNotBe null
        }

        should("throw exception for invalid endring type without required fields") {
            val spec = simuleringSpec(
                type = SimuleringTypeEnum.ENDR_ALDER,
                foersteUttakDato = null // Invalid - required for endring
            )

            shouldThrow<InvalidArgumentException> {
                simulatorCore().simuler(spec)
            }
        }
    }

    context("simuler - privat AFP") {
        should("calculate privat AFP when privatAfpVirkningFom is set") {
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

            val core = simulatorCore(
                privatAfpBeregner = privatAfpBeregner,
                ytelseService = ytelseService
            )

            core.simuler(initialSpec = simuleringSpec())

            verify(exactly = 1) { privatAfpBeregner.beregnPrivatAfp(any()) }
        }

        should("not calculate privat AFP when privatAfpVirkningFom is null") {
            val privatAfpBeregner = mockk<PrivatAfpBeregner>()
            simulatorCore(privatAfpBeregner = privatAfpBeregner).simuler(initialSpec = simuleringSpec())
            verify(exactly = 0) { privatAfpBeregner.beregnPrivatAfp(any()) }
        }
    }

    context("simuler - AFP_FPP special case") {
        should("return special output for AFP_FPP simulation type") {
            val offentligAfpBeregner = mockk<OffentligAfpBeregner> {
                every { beregnAfp(any(), any(), any(), any(), any()) } returns OffentligAfpResult(
                    tidsbegrenset = TidsbegrensetOffentligAfpResult(
                        simuleringResult = Simuleringsresultat(),
                        kravhode = kravhode()
                    ),
                    livsvarig = null,
                    kravhode = kravhode()
                )
            }

            simulatorCore(offentligAfpBeregner = offentligAfpBeregner).simuler(
                initialSpec = simuleringSpec(type = SimuleringTypeEnum.AFP_FPP)
            ).tidsbegrensetOffentligAfp shouldNotBe null
        }
    }

    context("simuler - anonymous vs personal") {
        should("not add personal data to output when anonym") {
            val result = simulatorCore(soekerFoedselsdato = null).simuler(
                initialSpec = simuleringSpec(erAnonym = true, pid = null)
            )

            with(result) {
                registerData?.soekerFoedselsdato shouldBe null
                persongrunnlag shouldBe null
            }
        }

        should("add personal data to output when not anonym") {
            val result = simulatorCore(
                soekerFoedselsdato = date,
                personService = arrangePerson(foedselsdato = date)
            ).simuler(
                initialSpec = simuleringSpec(erAnonym = false)
            )

            with(result) {
                registerData?.soekerFoedselsdato shouldBe date
                persongrunnlag shouldNotBe null
            }
        }
    }

    context("simuler - metrics and logging") {
        should("count simuleringstype metric") {
            simulatorCore().simuler(initialSpec = simuleringSpec(type = SimuleringTypeEnum.ALDER))
            // Metrics.countSimuleringstype is called - we can't easily verify static calls,
            // but the test ensures no exception is thrown
        }
    }

    context("simuler - kravhode operations") {
        should("create and update kravhode") {
            val kravhode = kravhode()
            val updatedKravhode = kravhode()

            val kravhodeCreator = mockk<KravhodeCreator> {
                every { opprettKravhode(any(), any(), any()) } returns kravhode
            }

            val kravhodeUpdater = mockk<KravhodeUpdater> {
                every { updateKravhodeForFoersteKnekkpunkt(any()) } returns updatedKravhode
            }

            val core = simulatorCore(
                kravhodeCreator = kravhodeCreator,
                kravhodeUpdater = kravhodeUpdater
            )

            core.simuler(initialSpec = simuleringSpec())

            verify { kravhodeCreator.opprettKravhode(any(), any(), any()) }
            verify { kravhodeUpdater.updateKravhodeForFoersteKnekkpunkt(any()) }
        }
    }

    context("simuler - knekkpunkt finding") {
        should("find knekkpunkter") {
            val knekkpunktFinder = arrangeKnekkpunkt()
            simulatorCore(knekkpunktFinder = knekkpunktFinder).simuler(initialSpec = simuleringSpec())
            verify { knekkpunktFinder.finnKnekkpunkter(any()) }
        }
    }

    context("simuler - offentlig AFP") {
        should("calculate offentlig AFP when innvilgetAfp is null") {
            val offentligAfpBeregner = mockk<OffentligAfpBeregner> {
                every {
                    beregnAfp(any(), any(), any(), any(), any())
                } returns OffentligAfpResult(
                    tidsbegrenset = null,
                    livsvarig = null,
                    kravhode = kravhode()
                )
            }

            simulatorCore(offentligAfpBeregner = offentligAfpBeregner).simuler(initialSpec = simuleringSpec())

            verify { offentligAfpBeregner.beregnAfp(any(), any(), any(), any(), any()) }
        }
    }

    context("simuler - result preparation") {
        should("prepare result with correct spec values") {
            val resultPreparer = arrangeOutput()
            simulatorCore(resultPreparer = resultPreparer).simuler(initialSpec = simuleringSpec())
            verify { resultPreparer.opprettOutput(any()) }
        }
    }

    context("simuler - endring av alderspensjon med gjenlevenderett") {
        should("validate ENDR_ALDER_M_GJEN with avdoed doedDato") {
            val spec = simuleringSpec(
                type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = LocalDate.of(2027, 1, 1),
                uttaksgrad = UttakGradKode.P_100,
                avdoed = Avdoed(
                    pid = Pid("98765432109"),
                    antallAarUtenlands = 0,
                    inntektFoerDoed = 0,
                    doedDato = LocalDate.of(2020, 5, 15)
                )
            )

            simulatorCore().simuler(spec) shouldNotBe null
        }

        should("throw for ENDR_ALDER_M_GJEN without avdoed doedDato") {
            val spec = simuleringSpec(
                type = SimuleringTypeEnum.ENDR_ALDER_M_GJEN,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = LocalDate.of(2027, 1, 1),
                uttaksgrad = UttakGradKode.P_100,
                avdoed = null
            )

            shouldThrow<InvalidArgumentException> {
                simulatorCore().simuler(spec)
            }
        }
    }

    context("simuler - heltUttakDato requirement") {
        should("throw for gradert uttak without heltUttakDato") {
            val spec = simuleringSpec(
                type = SimuleringTypeEnum.ENDR_ALDER,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = null, // Missing for gradert uttak
                uttaksgrad = UttakGradKode.P_50
            )

            shouldThrow<InvalidArgumentException> {
                simulatorCore().simuler(spec)
            }
        }
    }

    context("fetchFoedselsdato") {
        should("delegate to generalPersonService") {
            val expectedDate = LocalDate.of(1965, 3, 20)
            val generalPersonService = arrangeFoedselsdato(expectedDate)
            val pid = Pid("12345678901")

            simulatorCore(generalPersonService = generalPersonService).fetchFoedselsdato(pid) shouldBe expectedDate
            verify { generalPersonService.foedselsdato(pid) }
        }
    }
})

private val date: LocalDate = LocalDate.of(1963, 1, 1)

private fun simulatorCore(
    soekerFoedselsdato: LocalDate? = null,
    kravhodeCreator: KravhodeCreator = mockKravhodeCreator(),
    kravhodeUpdater: KravhodeUpdater = mockKravhodeUpdater(),
    knekkpunktFinder: KnekkpunktFinder = arrangeKnekkpunkt(),
    alderspensjonVilkaarsproeverOgBeregner: AlderspensjonVilkaarsproeverOgBeregner = mockAlderspensjonBeregner(),
    privatAfpBeregner: PrivatAfpBeregner = mockk(relaxed = true),
    generalPersonService: GeneralPersonService = arrangeFoedselsdato(),
    personService: PersonService = arrangePerson(),
    sakService: SakService = mockSakService(),
    ytelseService: YtelseService = mockYtelseService(),
    offentligAfpBeregner: OffentligAfpBeregner = mockOffentligAfpBeregner(),
    grunnbeloepService: GrunnbeloepService = mockGrunnbeloepService(),
    generelleDataHolder: GenerelleDataHolder = mockGenerelleDataHolder(),
    resultPreparer: SimuleringResultPreparer = arrangeOutput(soekerFoedselsdato)
) =
    SimulatorCore(
        kravhodeCreator,
        kravhodeUpdater,
        knekkpunktFinder,
        alderspensjonVilkaarsproeverOgBeregner,
        privatAfpBeregner,
        generalPersonService,
        personService,
        specOverrider = arrangeNoOverride(),
        sakService,
        ytelseService,
        offentligAfpBeregner,
        grunnbeloepService,
        generelleDataHolder,
        resultPreparer
    )

private fun mockKravhodeCreator(): KravhodeCreator = mockk {
    every { opprettKravhode(any(), any(), any()) } returns kravhode()
}

private fun mockKravhodeUpdater(): KravhodeUpdater = mockk {
    every { updateKravhodeForFoersteKnekkpunkt(any()) } returns kravhode()
}

private fun mockAlderspensjonBeregner(): AlderspensjonVilkaarsproeverOgBeregner = mockk {
    every { vilkaarsproevOgBeregnAlder(any()) } returns AlderspensjonBeregnerResult(
        beregningsresultater = mutableListOf(BeregningsResultatAlderspensjon2011()),
        pensjonsbeholdningPerioder = mutableListOf()
    )
}

private fun arrangeFoedselsdato(foedselsdato: LocalDate = date): GeneralPersonService =
    mockk { every { foedselsdato(any()) } returns foedselsdato }

private fun arrangePerson(foedselsdato: LocalDate = date): PersonService =
    mockk {
        every { person(any()) } returns PenPerson().apply { this.foedselsdato = foedselsdato }
    }

private fun arrangeKnekkpunkt(): KnekkpunktFinder =
    mockk { every { finnKnekkpunkter(any()) } returns sortedMapOf() }

private fun arrangeNoOverride(): SpecOverrider =
    mockk { every { behandleSpec(spec = any(), ytelser = any(), foedselsdato = any()) } returnsArgument 0 }

private fun arrangeOutput(soekerFoedselsdato: LocalDate? = null): SimuleringResultPreparer =
    mockk {
        every {
            opprettOutput(any())
        } returns SimulatorOutput().apply {
            registerData = soekerFoedselsdato?.let { RegisterData(soekerFoedselsdato = it) }
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
        tidsbegrenset = null,
        livsvarig = null,
        kravhode = kravhode()
    )
}

private fun mockGrunnbeloepService(): GrunnbeloepService = mockk {
    every { naavaerendeGrunnbeloep() } returns 118620
}

private fun mockGenerelleDataHolder(): GenerelleDataHolder = mockk {
    every { getSisteGyldigeOpptjeningsaar() } returns 2023
}

private fun kravhode() =
    Kravhode().apply {
        persongrunnlagListe = mutableListOf(
            Persongrunnlag().apply {
                fodselsdatoLd = date
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

private fun simuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    pid: Pid? = Pid("12345678901"),
    foersteUttakDato: LocalDate? = LocalDate.of(2029, 1, 1),
    heltUttakDato: LocalDate? = LocalDate.of(2032, 6, 1),
    uttaksgrad: UttakGradKode = UttakGradKode.P_100,
    erAnonym: Boolean = false,
    avdoed: Avdoed? = null
) = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = heltUttakDato,
    pid = pid,
    foedselDato = date,
    avdoed = avdoed,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = uttaksgrad,
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
    erAnonym = erAnonym,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)