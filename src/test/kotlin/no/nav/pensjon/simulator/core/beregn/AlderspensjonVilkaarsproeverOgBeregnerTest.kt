package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpGrunnlagService
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.knekkpunkt.KnekkpunktAarsak
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.core.vilkaar.Vilkaarsproever
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.trygdetid.TrygdetidBeregnerProxy
import no.nav.pensjon.simulator.trygdetid.TrygdetidCombo
import java.time.LocalDate
import java.util.*

class AlderspensjonVilkaarsproeverOgBeregnerTest : FunSpec({

    // ===========================================
    // Tests for AFP_FPP simulation type
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should return empty result for AFP_FPP simulation type") {
        val beregner = createBeregner()

        val spec = createSpec(
            simulering = simuleringSpec(type = SimuleringTypeEnum.AFP_FPP)
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        result.beregningsresultater.shouldBeEmpty()
        result.pensjonsbeholdningPerioder.shouldBeEmpty()
    }

    // ===========================================
    // Tests for basic knekkpunkt processing
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should process single UTG knekkpunkt") {
        val expectedResult = BeregningsResultatAlderspensjon2011()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        val kravhode = createKravhodeWithSoker()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedResult
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        result.beregningsresultater shouldHaveSize 1
        result.beregningsresultater[0] shouldBe expectedResult
        verify(exactly = 1) { vilkaarsproever.vilkaarsproevKrav(any()) }
        verify(exactly = 1) { alderspensjonBeregner.beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    test("vilkaarsproevOgBeregnAlder should process multiple knekkpunkter") {
        val result1 = BeregningsResultatAlderspensjon2011()
        val result2 = BeregningsResultatAlderspensjon2011()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        val kravhode = createKravhodeWithSoker()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(result1, result2)
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns SisteAldersberegning2011()
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG),
                LocalDate.of(2026, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        result.beregningsresultater shouldHaveSize 2
        verify(exactly = 2) { vilkaarsproever.vilkaarsproevKrav(any()) }
        verify(exactly = 2) { alderspensjonBeregner.beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    // ===========================================
    // Tests for onlyVilkaarsproeving
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should return early when onlyVilkaarsproeving is true") {
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        val kravhode = createKravhodeWithSoker()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner>()
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator>(relaxed = true)
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            ),
            onlyVilkaarsproeving = true
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        result.beregningsresultater.shouldBeEmpty()
        verify(exactly = 1) { vilkaarsproever.vilkaarsproevKrav(any()) }
        verify(exactly = 0) { alderspensjonBeregner.beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    // ===========================================
    // Tests for trygdetid updates
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should update trygdetid for soker when TTBRUKER knekkpunkt") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy> {
            every { fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returns TrygdetidCombo(null, null)
        }
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG, KnekkpunktAarsak.TTBRUKER)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        verify(exactly = 1) { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), eq(GrunnlagsrolleEnum.SOKER), any(), any()) }
    }

    test("vilkaarsproevOgBeregnAlder should update trygdetid for avdod when TTAVDOD knekkpunkt") {
        val avdodGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD)
        val kravhode = createKravhodeWithSoker().apply {
            persongrunnlagListe.add(avdodGrunnlag)
        }
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy> {
            every { fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returns TrygdetidCombo(null, null)
        }
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG, KnekkpunktAarsak.TTAVDOD)
            ),
            avdodForsteVirk = LocalDate.of(2020, 1, 1)
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        verify(exactly = 1) { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), eq(GrunnlagsrolleEnum.AVDOD), any(), any()) }
    }

    // ===========================================
    // Tests for vilkaarsproving trigger
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should only vilkaarsprov when UTG knekkpunkt present") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy> {
            every { fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returns TrygdetidCombo(null, null)
        }
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        // Knekkpunkt without UTG - should not trigger vilkaarsproving
        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.TTBRUKER)
            ),
            forrigeVilkarsvedtakListe = mutableListOf(VilkarsVedtak())
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        verify(exactly = 0) { vilkaarsproever.vilkaarsproevKrav(any()) }
        verify(exactly = 1) { alderspensjonBeregner.beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    // ===========================================
    // Tests for sisteBeregning handling
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should use sisteBeregning for revurdering") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        val sisteBeregning = SisteAldersberegning2011()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns sisteBeregning
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG),
                LocalDate.of(2026, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // First call should be foersteUttak=true, second should be foersteUttak=false
        verify(exactly = 1) { alderspensjonBeregner.beregnAlderspensjon(any(), any(), any(), isNull(), any(), any(), any(), any(), eq(true), any()) }
        verify(exactly = 1) { alderspensjonBeregner.beregnAlderspensjon(any(), any(), any(), eq(sisteBeregning), any(), any(), any(), any(), eq(false), any()) }
    }

    // ===========================================
    // Tests for virkTom setting
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should set virkTom on beregningsresultat from previous iteration") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        // This result will be returned by beregner in first iteration
        // and should have virkTom set in second iteration
        val firstIterationResult = BeregningsResultatAlderspensjon2011()
        val secondIterationResult = BeregningsResultatAlderspensjon2011()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(firstIterationResult, secondIterationResult)
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns SisteAldersberegning2011()
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG),
                LocalDate.of(2026, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // The result from first iteration should have virkTom set to day before second knekkpunkt
        // virkTom is set on forrigeAlderspensjonBeregningResultat (line 89) which is the result from previous iteration
        firstIterationResult.virkTom shouldNotBe null
    }

    // ===========================================
    // Tests for sisteGyldigeOpptjeningsAr update
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should update sisteGyldigeOpptjeningsAr on persongrunnlag") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // sisteGyldigeOpptjeningsAr should be knekkpunkt year - 2 (OPPTJENING_ETTERSLEP_ANTALL_AAR)
        kravhode.persongrunnlagListe[0].sisteGyldigeOpptjeningsAr shouldBe 2023
    }

    // ===========================================
    // Tests for ignoreAvslag
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should pass ignoreAvslag to beregner") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        var capturedIgnoreAvslag: Boolean? = null

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } answers {
                capturedIgnoreAvslag = arg(9)
                BeregningsResultatAlderspensjon2011()
            }
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            ),
            ignoreAvslag = true
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        capturedIgnoreAvslag shouldBe true
    }

    // ===========================================
    // Tests for livsvarig offentlig AFP
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should pass livsvarig offentlig AFP to beregner") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        val offentligAfp = AfpOffentligLivsvarigGrunnlag(sistRegulertG = 100000, bruttoPerAr = 50000.0)

        var capturedOffentligAfp: AfpOffentligLivsvarigGrunnlag? = null

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } answers {
                capturedOffentligAfp = arg(5)
                BeregningsResultatAlderspensjon2011()
            }
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockk<LivsvarigOffentligAfpGrunnlagService> {
            every { livsvarigOffentligAfpGrunnlag(any(), any(), any(), any()) } returns offentligAfp
        }

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        capturedOffentligAfp shouldBe offentligAfp
    }

    // ===========================================
    // Tests for isCriteriaForDoingKap20ForSimulerForTpFullfilled (simulerForTp hack)
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should do additional Kap20 beregning when simulerForTp criteria are met") {
        // This test verifies the "hack" for TP integrations that can't parse Kap 20 results
        // When simulerForTp=true, heltUttakDato < normertPensjoneringsdato, and knekkpunktDato < heltUttakDato,
        // an additional beregning is performed with simulerForTp temporarily set to false

        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        var beregningCallCount = 0

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } answers {
                beregningCallCount++
                BeregningsResultatAlderspensjon2011()
            }
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns SisteAldersberegning2011()
        }
        // normertPensjoneringsdato returns 2030-01-01
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        // Create spec with simulerForTp=true and heltUttakDato=2029-06-01 (before 2030-01-01)
        val simSpec = simuleringSpec(heltUttakDato = LocalDate.of(2029, 6, 1)).apply {
            simulerForTp = true
        }

        val spec = createSpec(
            kravhode = kravhode,
            simulering = simSpec,
            knekkpunkter = sortedMapOf(
                // knekkpunktDato=2025-01-01 is before heltUttakDato=2029-06-01
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should be called twice: once for main beregning, once for the Kap20 hack
        beregningCallCount shouldBe 2
    }

    test("vilkaarsproevOgBeregnAlder should not do additional Kap20 beregning when simulerForTp is false") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        var beregningCallCount = 0

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } answers {
                beregningCallCount++
                BeregningsResultatAlderspensjon2011()
            }
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        // simulerForTp=false (default)
        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should only be called once (no additional Kap20 beregning)
        beregningCallCount shouldBe 1
    }

    test("vilkaarsproevOgBeregnAlder should not do additional Kap20 beregning when knekkpunktDato is after heltUttakDato") {
        val kravhode = createKravhodeWithSoker()
        val vedtakListe = mutableListOf<VilkarsVedtak>()
        var beregningCallCount = 0

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } answers {
                beregningCallCount++
                BeregningsResultatAlderspensjon2011()
            }
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        // heltUttakDato=2024-06-01, knekkpunktDato=2025-01-01 (knekkpunkt AFTER heltUttak)
        val simSpec = simuleringSpec(heltUttakDato = LocalDate.of(2024, 6, 1)).apply {
            simulerForTp = true
        }

        val spec = createSpec(
            kravhode = kravhode,
            simulering = simSpec,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should only be called once (condition not met)
        beregningCallCount shouldBe 1
    }

    // ===========================================
    // Tests for shouldAddPensjonBeholdningPerioder
    // ===========================================

    test("vilkaarsproevOgBeregnAlder should add pensjonsbeholdning perioder when criteria are met with 2025 result") {
        val kravhode = Kravhode().apply {
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ // Required for shouldAddPensjonBeholdningPerioder
            persongrunnlagListe = mutableListOf(createPersongrunnlag(GrunnlagsrolleEnum.SOKER))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            )
        }
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        // Create beholdninger for the result
        val beholdninger = Beholdninger().apply {
            beholdninger = listOf(
                Pensjonsbeholdning().apply {
                    totalbelop = 1000000.0
                }
            )
        }

        val beregningsresultat2025 = BeregningsResultatAlderspensjon2025().apply {
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            beregningKapittel20 = AldersberegningKapittel20().apply {
                beholdningerForForsteuttak = beholdninger
            }
        }

        val context = mockk<SimulatorContext> {
            every { beregnOpptjening(any(), any(), any()) } returns mutableListOf()
        }
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            // First call returns 2011 result (main beregning), second call returns 2025 result (beholdning beregning)
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
                BeregningsResultatAlderspensjon2011(),
                beregningsresultat2025
            )
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
            every { innvilgetVedtak(any(), any()) } returns VilkarsVedtak()
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            ),
            isHentPensjonsbeholdninger = true
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should have one beholdning periode added
        result.pensjonsbeholdningPerioder shouldHaveSize 1
        result.pensjonsbeholdningPerioder[0].datoFom shouldBe LocalDate.of(2025, 1, 1)
    }

    test("vilkaarsproevOgBeregnAlder should add pensjonsbeholdning perioder when criteria are met with 2016 result") {
        val kravhode = Kravhode().apply {
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ // Also valid for shouldAddPensjonBeholdningPerioder
            persongrunnlagListe = mutableListOf(createPersongrunnlag(GrunnlagsrolleEnum.SOKER))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            )
        }
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        // Create beholdninger for the result
        val beholdninger = Beholdninger().apply {
            beholdninger = listOf(
                Pensjonsbeholdning().apply {
                    totalbelop = 500000.0
                }
            )
        }

        val beregningsresultat2016 = BeregningsResultatAlderspensjon2016().apply {
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            beregningsResultat2025 = BeregningsResultatAlderspensjon2025().apply {
                beregningKapittel20 = AldersberegningKapittel20().apply {
                    beholdningerForForsteuttak = beholdninger
                }
            }
        }

        val context = mockk<SimulatorContext> {
            every { beregnOpptjening(any(), any(), any()) } returns mutableListOf()
        }
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            // First call returns 2011 result (main beregning), second call returns 2016 result (beholdning beregning)
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
                BeregningsResultatAlderspensjon2011(),
                beregningsresultat2016
            )
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
            every { innvilgetVedtak(any(), any()) } returns VilkarsVedtak()
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            ),
            isHentPensjonsbeholdninger = true
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should have one beholdning periode added
        result.pensjonsbeholdningPerioder shouldHaveSize 1
    }

    test("vilkaarsproevOgBeregnAlder should not add pensjonsbeholdning perioder when isHentPensjonsbeholdninger is false") {
        val kravhode = Kravhode().apply {
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
            persongrunnlagListe = mutableListOf(createPersongrunnlag(GrunnlagsrolleEnum.SOKER))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            )
        }
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            ),
            isHentPensjonsbeholdninger = false // Disabled
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should have no beholdning perioder
        result.pensjonsbeholdningPerioder.shouldBeEmpty()
    }

    test("vilkaarsproevOgBeregnAlder should not add pensjonsbeholdning perioder when regelverkType is not new opptjening") {
        // Using N_REG_G_OPPTJ which is NOT in the list of allowed types (N_REG_N_OPPTJ, N_REG_G_N_OPPTJ)
        val kravhode = createKravhodeWithSoker() // Uses N_REG_G_OPPTJ
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            ),
            isHentPensjonsbeholdninger = true // Enabled, but regelverkType is wrong
        )

        val result = beregner.vilkaarsproevOgBeregnAlder(spec)

        // Should have no beholdning perioder
        result.pensjonsbeholdningPerioder.shouldBeEmpty()
    }

    // ===========================================
    // Tests for personDetalj filtering (periodiserGrunnlag)
    // ===========================================

    // Note: The periodiserDetaljer function in AlderspensjonVilkaarsproeverOgBeregner has a bug
    // where it modifies the list while iterating (lines 518-524), causing ConcurrentModificationException.
    // The filtering functionality cannot be properly tested due to this bug.
    // If there are no personDetalj elements with bruk=false, no removal is attempted and no error occurs.
    test("vilkaarsproevOgBeregnAlder should keep personDetalj with bruk=true") {
        val kravhode = Kravhode().apply {
            regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            persongrunnlagListe = mutableListOf(
                Persongrunnlag().apply {
                    fodselsdato = dateAtNoon(1960, Calendar.JANUARY, 1)
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
        val vedtakListe = mutableListOf<VilkarsVedtak>()

        val context = mockk<SimulatorContext>(relaxed = true)
        val alderspensjonBeregner = mockk<AlderspensjonBeregner> {
            every { beregnAlderspensjon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns BeregningsResultatAlderspensjon2011()
        }
        val vilkaarsproever = mockk<Vilkaarsproever> {
            every { vilkaarsproevKrav(any()) } returns Tuple2(vedtakListe, kravhode)
        }
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
        val sisteBeregningCreator = mockk<SisteBeregningCreator> {
            every { opprettSisteBeregning(any(), any(), any()) } returns null
        }
        val normalderService = mockNormalderService()
        val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

        val beregner = AlderspensjonVilkaarsproeverOgBeregner(
            context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
            sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
        )

        val spec = createSpec(
            kravhode = kravhode,
            knekkpunkter = sortedMapOf(
                LocalDate.of(2025, 1, 1) to mutableListOf(KnekkpunktAarsak.UTG)
            )
        )

        beregner.vilkaarsproevOgBeregnAlder(spec)

        // The detalj with bruk=true should be kept
        kravhode.persongrunnlagListe[0].personDetaljListe shouldHaveSize 1
        kravhode.persongrunnlagListe[0].personDetaljListe[0].grunnlagsrolleEnum shouldBe GrunnlagsrolleEnum.SOKER
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun mockNormalderService(): NormertPensjonsalderService =
    mockk {
        every { normalder(any<LocalDate>()) } returns Alder(67, 0)
        every { normertPensjoneringsdato(any()) } returns LocalDate.of(2030, 1, 1)
    }

private fun mockLivsvarigOffentligAfpService(): LivsvarigOffentligAfpGrunnlagService =
    mockk {
        every { livsvarigOffentligAfpGrunnlag(any(), any(), any(), any()) } returns null
    }

private fun createBeregner(): AlderspensjonVilkaarsproeverOgBeregner {
    val context = mockk<SimulatorContext>(relaxed = true)
    val alderspensjonBeregner = mockk<AlderspensjonBeregner>(relaxed = true)
    val vilkaarsproever = mockk<Vilkaarsproever>(relaxed = true)
    val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>(relaxed = true)
    val sisteBeregningCreator = mockk<SisteBeregningCreator>(relaxed = true)
    val normalderService = mockNormalderService()
    val livsvarigOffentligAfpService = mockLivsvarigOffentligAfpService()

    return AlderspensjonVilkaarsproeverOgBeregner(
        context, alderspensjonBeregner, vilkaarsproever, trygdetidBeregner,
        sisteBeregningCreator, normalderService, livsvarigOffentligAfpService
    )
}

private fun createSpec(
    kravhode: Kravhode = createKravhodeWithSoker(),
    knekkpunkter: SortedMap<LocalDate, MutableList<KnekkpunktAarsak>> = sortedMapOf(),
    simulering: no.nav.pensjon.simulator.core.spec.SimuleringSpec = simuleringSpec(),
    sokerForsteVirk: LocalDate = LocalDate.of(2025, 1, 1),
    avdodForsteVirk: LocalDate? = null,
    forrigeVilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    forrigeAlderBeregningsresultat: AbstraktBeregningsResultat? = null,
    sisteBeregning: SisteBeregning? = null,
    afpPrivatBeregningsresultater: MutableList<BeregningsResultatAfpPrivat> = mutableListOf(),
    gjeldendeAfpPrivatBeregningsresultat: BeregningsResultatAfpPrivat? = null,
    forsteVirkAfpPrivat: LocalDate? = null,
    isHentPensjonsbeholdninger: Boolean = false,
    kravGjelder: KravGjelder = KravGjelder.FORSTEG_BH,
    sakId: Long? = null,
    sakType: SakTypeEnum? = null,
    ignoreAvslag: Boolean = false,
    onlyVilkaarsproeving: Boolean = false
) = AlderspensjonVilkaarsproeverBeregnerSpec(
    kravhode = kravhode,
    knekkpunkter = knekkpunkter,
    simulering = simulering,
    sokerForsteVirk = sokerForsteVirk,
    avdodForsteVirk = avdodForsteVirk,
    forrigeVilkarsvedtakListe = forrigeVilkarsvedtakListe,
    forrigeAlderBeregningsresultat = forrigeAlderBeregningsresultat,
    sisteBeregning = sisteBeregning,
    afpPrivatBeregningsresultater = afpPrivatBeregningsresultater,
    gjeldendeAfpPrivatBeregningsresultat = gjeldendeAfpPrivatBeregningsresultat,
    forsteVirkAfpPrivat = forsteVirkAfpPrivat,
    isHentPensjonsbeholdninger = isHentPensjonsbeholdninger,
    kravGjelder = kravGjelder,
    sakId = sakId,
    sakType = sakType,
    afpOffentligLivsvarigBeregningsresultat = null,
    ignoreAvslag = ignoreAvslag,
    onlyVilkaarsproeving = onlyVilkaarsproeving
)

private fun createKravhodeWithSoker(): Kravhode {
    val sokerGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER)
    return Kravhode().apply {
        regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
        persongrunnlagListe = mutableListOf(sokerGrunnlag)
        kravlinjeListe = mutableListOf(
            Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
        )
    }
}

private fun createPersongrunnlag(rolle: GrunnlagsrolleEnum): Persongrunnlag =
    Persongrunnlag().apply {
        fodselsdato = dateAtNoon(1960, Calendar.JANUARY, 1)
        penPerson = PenPerson().apply { penPersonId = 1L }
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = rolle
                virkFom = dateAtNoon(2020, Calendar.JANUARY, 1)
            }
        )
    }
