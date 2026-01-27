package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpBeholdning
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagSpec
import no.nav.pensjon.simulator.trygdetid.TrygdetidSetter
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate
import java.util.*

class KravhodeUpdaterTest : FunSpec({

    test("oppdatere kravhodets persongrunnlag med trygdeavtale der kravDatoIAvtaleland = dagens dato") {
        val idag = LocalDate.of(2025, 1, 1)
        val kravhode = createKravhodeUpdater(today = idag).updateKravhodeForFoersteKnekkpunkt(
            spec = createUpdateSpec(
                regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
                utlandPeriodeListe = utlandPeriodeListe()
            )
        )

        with(kravhode) {
            persongrunnlagListe shouldHaveSize 1
            persongrunnlagListe[0].trygdeavtale!!.kravDatoIAvtaleland shouldBe idag.toNorwegianDateAtNoon()
        }
    }

    // =====================================================
    // Tests for trygdeavtale setup when boddUtenlands
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should set trygdeavtale with kravDatoIAvtaleland as today") {
        val today = LocalDate.of(2025, 1, 15)
        val updater = createKravhodeUpdater(today = today)
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val soeker = result.hentPersongrunnlagForSoker()
        soeker.trygdeavtale.shouldNotBeNull()
        soeker.trygdeavtale!!.kravDatoIAvtaleland shouldBe today.toNorwegianDateAtNoon()
    }

    test("updateKravhodeForFoersteKnekkpunkt should set trygdeavtaledetaljer when boddUtenlands") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        result.hentPersongrunnlagForSoker().trygdeavtaledetaljer.shouldNotBeNull()
    }

    test("updateKravhodeForFoersteKnekkpunkt should set inngangOgEksportGrunnlag when boddUtenlands") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        result.hentPersongrunnlagForSoker().inngangOgEksportGrunnlag.shouldNotBeNull()
    }

    test("updateKravhodeForFoersteKnekkpunkt should set boddEllerArbeidetIUtlandet on kravhode when boddUtenlands") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        result.boddEllerArbeidetIUtlandet shouldBe true
    }

    // =====================================================
    // Tests for kapittel 19/20 trygdetid based on regelverkType
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should add only kapittel 19 trygdetid for N_REG_G_OPPTJ (2011)") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_G_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val soeker = result.hentPersongrunnlagForSoker()
        soeker.trygdetidPerioder.shouldNotBeNull()
        soeker.trygdetidPerioder.isEmpty() shouldBe false
        soeker.trygdetidPerioderKapittel20.shouldBeEmpty()
    }

    test("updateKravhodeForFoersteKnekkpunkt should add both kapittel 19 and 20 trygdetid for N_REG_G_N_OPPTJ (2016)") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val soeker = result.hentPersongrunnlagForSoker()
        soeker.trygdetidPerioder.shouldNotBeNull()
        soeker.trygdetidPerioder.isEmpty() shouldBe false
        soeker.trygdetidPerioderKapittel20.shouldNotBeNull()
        soeker.trygdetidPerioderKapittel20.isEmpty() shouldBe false
    }

    test("updateKravhodeForFoersteKnekkpunkt should add only kapittel 20 trygdetid for N_REG_N_OPPTJ (2025)") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val soeker = result.hentPersongrunnlagForSoker()
        soeker.trygdetidPerioder.shouldBeEmpty()
        soeker.trygdetidPerioderKapittel20.shouldNotBeNull()
        soeker.trygdetidPerioderKapittel20.isEmpty() shouldBe false
    }

    // =====================================================
    // Tests for anonymous simulation trygdetid
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should add anonymous trygdetid period when erAnonym is true") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            erAnonym = true,
            utlandPeriodeListe = emptyList()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val soeker = result.hentPersongrunnlagForSoker()
        soeker.trygdetidPerioder shouldHaveSize 1
        soeker.trygdetidPerioderKapittel20 shouldHaveSize 1
    }

    test("updateKravhodeForFoersteKnekkpunkt should not set trygdeavtale for anonymous simulation") {
        val updater = createKravhodeUpdater()
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            erAnonym = true,
            utlandPeriodeListe = emptyList()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        result.hentPersongrunnlagForSoker().trygdeavtale.shouldBeNull()
    }

    // =====================================================
    // Tests for trygdetidSetter delegation
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should call trygdetidSetter when not boddUtenlands and not anonymous") {
        val trygdetidSetter = mockk<TrygdetidSetter>()
        every { trygdetidSetter.settTrygdetid(any()) } answers {
            firstArg<TrygdetidGrunnlagSpec>().persongrunnlag
        }

        val updater = createKravhodeUpdater(trygdetidSetter = trygdetidSetter)
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            utlandPeriodeListe = emptyList(),
            erAnonym = false
        )

        updater.updateKravhodeForFoersteKnekkpunkt(spec)

        verify(exactly = 1) { trygdetidSetter.settTrygdetid(any()) }
    }

    // =====================================================
    // Tests for pensjonsbeholdning
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should call context.beregnOpptjening for standard simulation") {
        val context = mockk<SimulatorContext>()
        every { context.beregnOpptjening(any(), any(), any()) } returns mutableListOf(
            Pensjonsbeholdning().apply { beholdningsTypeEnum = BeholdningtypeEnum.PEN_B }
        )

        val updater = createKravhodeUpdater(context = context)
        val spec = createUpdateSpec(
            type = SimuleringTypeEnum.ALDER,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ
        )

        updater.updateKravhodeForFoersteKnekkpunkt(spec)

        verify(exactly = 1) { context.beregnOpptjening(any(), any(), any()) }
    }

    test("updateKravhodeForFoersteKnekkpunkt should filter beholdninger to only PEN_B type") {
        val context = mockk<SimulatorContext>()
        every { context.beregnOpptjening(any(), any(), any()) } returns mutableListOf(
            Pensjonsbeholdning().apply { beholdningsTypeEnum = BeholdningtypeEnum.PEN_B },
            Pensjonsbeholdning().apply { beholdningsTypeEnum = BeholdningtypeEnum.GAR_PEN_B },
            Pensjonsbeholdning().apply { beholdningsTypeEnum = BeholdningtypeEnum.PEN_B }
        )

        val updater = createKravhodeUpdater(context = context)
        val spec = createUpdateSpec(
            type = SimuleringTypeEnum.ALDER,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        result.hentPersongrunnlagForSoker().beholdninger shouldHaveSize 2
    }

    test("updateKravhodeForFoersteKnekkpunkt should call pre2025OffentligAfpBeholdning for AFP_ETTERF_ALDER") {
        val afpBeholdning = mockk<Pre2025OffentligAfpBeholdning>()
        every { afpBeholdning.setPensjonsbeholdning(any(), any()) } returns mockk()

        val updater = createKravhodeUpdater(tidsbegrensetOffentligAfpBeholdning = afpBeholdning)
        val spec = createUpdateSpec(
            type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ
        )

        updater.updateKravhodeForFoersteKnekkpunkt(spec)

        verify(exactly = 1) { afpBeholdning.setPensjonsbeholdning(any(), any()) }
    }

    test("updateKravhodeForFoersteKnekkpunkt should not call context.beregnOpptjening for endring simulation") {
        val context = mockk<SimulatorContext>()

        val updater = createKravhodeUpdater(context = context)
        val spec = createUpdateSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ
        )

        updater.updateKravhodeForFoersteKnekkpunkt(spec)

        verify(exactly = 0) { context.beregnOpptjening(any(), any()) }
    }

    // =====================================================
    // Tests for uførehistorikk
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should set ufgTom on uforeperioder when not already set") {
        val updater = createKravhodeUpdater()
        val persongrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER).apply {
            uforeHistorikk = Uforehistorikk().apply {
                uforeperiodeListe = mutableListOf(
                    Uforeperiode().apply {
                        ufgFom = dateAtNoon(2010, Calendar.JANUARY, 1)
                        ufgTom = null // Not set
                    }
                )
            }
        }
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            persongrunnlag = persongrunnlag
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val uforeperiode = result.hentPersongrunnlagForSoker().uforeHistorikk?.uforeperiodeListe?.first()
        uforeperiode?.ufgTom.shouldNotBeNull()
    }

    test("updateKravhodeForFoersteKnekkpunkt should not modify ufgTom when already set") {
        val existingTom = dateAtNoon(2020, Calendar.DECEMBER, 31)
        val updater = createKravhodeUpdater()
        val persongrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER).apply {
            uforeHistorikk = Uforehistorikk().apply {
                uforeperiodeListe = mutableListOf(
                    Uforeperiode().apply {
                        ufgFom = dateAtNoon(2010, Calendar.JANUARY, 1)
                        ufgTom = existingTom
                    }
                )
            }
        }
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            persongrunnlag = persongrunnlag
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val uforeperiode = result.hentPersongrunnlagForSoker().uforeHistorikk?.uforeperiodeListe?.first()
        uforeperiode?.ufgTom shouldBe existingTom
    }

    test("updateKravhodeForFoersteKnekkpunkt should use foersteUttakDato minus 1 day for ALDER_M_AFP_PRIVAT when before normalder") {
        val foersteUttakDato = LocalDate.of(2028, 6, 1)
        val normalderDato = LocalDate.of(2030, 2, 1) // After foersteUttakDato

        val updater = createKravhodeUpdater(normalderAlder = Alder(67, 1))
        val persongrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER).apply {
            uforeHistorikk = Uforehistorikk().apply {
                uforeperiodeListe = mutableListOf(
                    Uforeperiode().apply {
                        ufgFom = dateAtNoon(2010, Calendar.JANUARY, 1)
                        ufgTom = null
                    }
                )
            }
        }
        val spec = createUpdateSpec(
            type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            persongrunnlag = persongrunnlag,
            foersteUttakDato = foersteUttakDato
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val uforeperiode = result.hentPersongrunnlagForSoker().uforeHistorikk?.uforeperiodeListe?.first()
        uforeperiode?.ufgTom shouldBe foersteUttakDato.minusDays(1).toNorwegianDateAtNoon()
    }

    // =====================================================
    // Tests for avdød handling
    // =====================================================

    test("updateKravhodeForFoersteKnekkpunkt should set trygdetid for avdod when present") {
        val updater = createKravhodeUpdater()
        val avdoedGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD).apply {
            dodsdato = dateAtNoon(2020, Calendar.DECEMBER, 15)
        }
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
            avdoedGrunnlag = avdoedGrunnlag,
            utlandPeriodeListe = utlandPeriodeListe()
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val avdoed = result.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.AVDOD, false)
        avdoed.shouldNotBeNull()
        avdoed.trygdeavtale.shouldNotBeNull()
    }

    test("updateKravhodeForFoersteKnekkpunkt should set uforehistorikk for avdod with dodsdato as tom") {
        val dodsdato = dateAtNoon(2020, Calendar.DECEMBER, 15)
        val updater = createKravhodeUpdater()
        val avdoedGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD).apply {
            this.dodsdato = dodsdato
            uforeHistorikk = Uforehistorikk().apply {
                uforeperiodeListe = mutableListOf(
                    Uforeperiode().apply {
                        ufgFom = dateAtNoon(2010, Calendar.JANUARY, 1)
                        ufgTom = null
                    }
                )
            }
        }
        val spec = createUpdateSpec(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            avdoedGrunnlag = avdoedGrunnlag
        )

        val result = updater.updateKravhodeForFoersteKnekkpunkt(spec)

        val avdoed = result.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.AVDOD, false)
        val uforeperiode = avdoed?.uforeHistorikk?.uforeperiodeListe?.first()
        uforeperiode?.ufgTom shouldBe dodsdato
    }

    // =====================================================
    // Tests for RegelverkTypeEnum extension properties
    // =====================================================

    test("isAlderspensjon2011 should be true for N_REG_G_OPPTJ") {
        RegelverkTypeEnum.N_REG_G_OPPTJ.isAlderspensjon2011 shouldBe true
    }

    test("isAlderspensjon2011 should be false for N_REG_G_N_OPPTJ") {
        RegelverkTypeEnum.N_REG_G_N_OPPTJ.isAlderspensjon2011 shouldBe false
    }

    test("isAlderspensjon2011 should be false for N_REG_N_OPPTJ") {
        RegelverkTypeEnum.N_REG_N_OPPTJ.isAlderspensjon2011 shouldBe false
    }

    test("isAlderspensjon2016 should be true for N_REG_G_N_OPPTJ") {
        RegelverkTypeEnum.N_REG_G_N_OPPTJ.isAlderspensjon2016 shouldBe true
    }

    test("isAlderspensjon2016 should be false for N_REG_G_OPPTJ") {
        RegelverkTypeEnum.N_REG_G_OPPTJ.isAlderspensjon2016 shouldBe false
    }

    test("isAlderspensjon2016 should be false for N_REG_N_OPPTJ") {
        RegelverkTypeEnum.N_REG_N_OPPTJ.isAlderspensjon2016 shouldBe false
    }

    test("isAlderspensjon2025 should be true for N_REG_N_OPPTJ") {
        RegelverkTypeEnum.N_REG_N_OPPTJ.isAlderspensjon2025 shouldBe true
    }

    test("isAlderspensjon2025 should be false for N_REG_G_OPPTJ") {
        RegelverkTypeEnum.N_REG_G_OPPTJ.isAlderspensjon2025 shouldBe false
    }

    test("isAlderspensjon2025 should be false for N_REG_G_N_OPPTJ") {
        RegelverkTypeEnum.N_REG_G_N_OPPTJ.isAlderspensjon2025 shouldBe false
    }

    test("isAlderspensjon2011 should be false for G_REG") {
        RegelverkTypeEnum.G_REG.isAlderspensjon2011 shouldBe false
    }

    test("isAlderspensjon2016 should be false for G_REG") {
        RegelverkTypeEnum.G_REG.isAlderspensjon2016 shouldBe false
    }

    test("isAlderspensjon2025 should be false for G_REG") {
        RegelverkTypeEnum.G_REG.isAlderspensjon2025 shouldBe false
    }
})

// =====================================================
// Helper functions
// =====================================================

private fun createKravhodeUpdater(
    context: SimulatorContext = mockk(relaxed = true),
    normalderAlder: Alder = Alder(67, 0),
    tidsbegrensetOffentligAfpBeholdning: Pre2025OffentligAfpBeholdning = mockk(),
    trygdetidSetter: TrygdetidSetter = mockk {
        every { settTrygdetid(any()) } answers { firstArg<TrygdetidGrunnlagSpec>().persongrunnlag }
    },
    today: LocalDate = LocalDate.of(2025, 1, 1)
): KravhodeUpdater {
    val normalderService = mockk<NormertPensjonsalderService>()
    every { normalderService.normalder(any<LocalDate>()) } returns normalderAlder

    return KravhodeUpdater(
        context = context,
        normalderService = normalderService,
        tidsbegrensetOffentligAfpBeholdning = tidsbegrensetOffentligAfpBeholdning,
        trygdetidSetter = trygdetidSetter,
        time = { today }
    )
}

private fun createUpdateSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    regelverkType: RegelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ,
    persongrunnlag: Persongrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER),
    avdoedGrunnlag: Persongrunnlag? = null,
    utlandPeriodeListe: List<UtlandPeriode> = emptyList(),
    erAnonym: Boolean = false,
    foersteUttakDato: LocalDate = LocalDate.of(2029, 1, 1)
): KravhodeUpdateSpec {
    val kravhode = Kravhode().apply {
        persongrunnlagListe = mutableListOf(persongrunnlag)
        avdoedGrunnlag?.let { persongrunnlagListe.add(it) }
        regelverkTypeEnum = regelverkType
    }

    return KravhodeUpdateSpec(
        kravhode = kravhode,
        simulering = createSimuleringSpec(
            type = type,
            utlandPeriodeListe = utlandPeriodeListe,
            erAnonym = erAnonym,
            foersteUttakDato = foersteUttakDato
        ),
        forrigeAlderspensjonBeregningResult = null
    )
}

private fun createPersongrunnlag(rolle: GrunnlagsrolleEnum): Persongrunnlag =
    Persongrunnlag().apply {
        penPerson = PenPerson().apply { penPersonId = if (rolle == GrunnlagsrolleEnum.SOKER) 1L else 2L }
        fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = rolle
                penRolleTom = dateAtNoon(2050, Calendar.JANUARY, 1)
            }
        )
    }

private fun createSimuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    utlandPeriodeListe: List<UtlandPeriode> = emptyList(),
    erAnonym: Boolean = false,
    foersteUttakDato: LocalDate = LocalDate.of(2029, 1, 1)
): SimuleringSpec = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = LocalDate.of(2032, 6, 1),
    pid = if (erAnonym) null else Pid("12345678910"),
    foedselDato = if (erAnonym) null else LocalDate.of(1963, 1, 1),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_100,
    forventetInntektBeloep = 500000,
    inntektUnderGradertUttakBeloep = 0,
    inntektEtterHeltUttakBeloep = 0,
    inntektEtterHeltUttakAntallAar = 0,
    foedselAar = 1963,
    utlandAntallAar = utlandPeriodeListe.size,
    utlandPeriodeListe = utlandPeriodeListe.toMutableList(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = false,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = erAnonym,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = false,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)

private fun utlandPeriodeListe(): List<UtlandPeriode> = listOf(
    UtlandPeriode(
        fom = LocalDate.of(2010, 1, 1),
        tom = LocalDate.of(2012, 12, 31),
        land = LandkodeEnum.SWE,
        arbeidet = false
    )
)
