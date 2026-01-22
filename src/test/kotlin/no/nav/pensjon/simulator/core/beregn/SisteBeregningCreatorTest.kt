package no.nav.pensjon.simulator.core.beregn

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class SisteBeregningCreatorTest : FunSpec({

    // ===========================================
    // Tests for sisteBeregningCreator selection based on regelverkType
    // ===========================================

    test("opprettSisteBeregning should use alderspensjon2011SisteBeregningCreator for N_REG_G_OPPTJ") {
        val expectedResult = SisteAldersberegning2011()
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } returns expectedResult
        }
        val creator2016 = mockk<Alderspensjon2016SisteBeregningCreator>()
        val creator2025 = mockk<Alderspensjon2025SisteBeregningCreator>()
        val kravService = mockk<KravService>()

        val sisteBeregningCreator = SisteBeregningCreator(
            kravService, creator2011, creator2016, creator2025
        )

        val result = sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        result shouldBe expectedResult
        verify(exactly = 1) { creator2011.createBeregning(any(), any()) }
    }

    test("opprettSisteBeregning should use alderspensjon2016SisteBeregningCreator for N_REG_G_N_OPPTJ") {
        val expectedResult = SisteAldersberegning2016()
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_N_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator>()
        val creator2016 = mockk<Alderspensjon2016SisteBeregningCreator> {
            every { createBeregning(any(), any()) } returns expectedResult
        }
        val creator2025 = mockk<Alderspensjon2025SisteBeregningCreator>()
        val kravService = mockk<KravService>()

        val sisteBeregningCreator = SisteBeregningCreator(
            kravService, creator2011, creator2016, creator2025
        )

        val result = sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        result shouldBe expectedResult
        verify(exactly = 1) { creator2016.createBeregning(any(), any()) }
    }

    test("opprettSisteBeregning should use alderspensjon2025SisteBeregningCreator for N_REG_N_OPPTJ") {
        // Note: Alderspensjon2025SisteBeregningCreator returns SisteAldersberegning2011, not a 2025-specific type
        val expectedResult = SisteAldersberegning2011()
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_N_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator>()
        val creator2016 = mockk<Alderspensjon2016SisteBeregningCreator>()
        val creator2025 = mockk<Alderspensjon2025SisteBeregningCreator> {
            every { createBeregning(any(), any()) } returns expectedResult
        }
        val kravService = mockk<KravService>()

        val sisteBeregningCreator = SisteBeregningCreator(
            kravService, creator2011, creator2016, creator2025
        )

        val result = sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        result shouldBe expectedResult
        verify(exactly = 1) { creator2025.createBeregning(any(), any()) }
    }

    test("opprettSisteBeregning should throw for unexpected regelverkType") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.G_REG)
        val beregningsresultat = createBeregningsresultat()

        val sisteBeregningCreator = createSisteBeregningCreator()

        shouldThrow<RuntimeException> {
            sisteBeregningCreator.opprettSisteBeregning(
                kravhode = kravhode,
                vedtakListe = emptyList(),
                beregningResultat = beregningsresultat
            )
        }.message shouldBe "Unexpected regelverkType G_REG in SisteBeregningCreator"
    }

    test("opprettSisteBeregning should throw for null regelverkType") {
        val kravhode = createKravhodeWithSoker(regelverkType = null)
        val beregningsresultat = createBeregningsresultat()

        val sisteBeregningCreator = createSisteBeregningCreator()

        shouldThrow<RuntimeException> {
            sisteBeregningCreator.opprettSisteBeregning(
                kravhode = kravhode,
                vedtakListe = emptyList(),
                beregningResultat = beregningsresultat
            )
        }.message shouldBe "Unexpected regelverkType null in SisteBeregningCreator"
    }

    // ===========================================
    // Tests for validation
    // ===========================================

    test("opprettSisteBeregning should throw when beregningsresultat is null") {
        val kravhode = createKravhodeWithSoker()

        val sisteBeregningCreator = createSisteBeregningCreator()

        shouldThrow<IllegalArgumentException> {
            sisteBeregningCreator.opprettSisteBeregning(
                kravhode = kravhode,
                vedtakListe = emptyList(),
                beregningResultat = null
            )
        }.message shouldBe "beregning and beregningsresultat cannot both be null"
    }

    // ===========================================
    // Tests for findForrigeKravhode
    // ===========================================

    test("opprettSisteBeregning should use periodized kravhode from spec when kravId matches") {
        // When beregningsresultat.kravId == kravhode.kravId, forrigeKravhode should come from spec.kravhode
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ).apply {
            kravId = 123L
        }
        val beregningsresultat = createBeregningsresultat().apply {
            kravId = 123L // Same as kravhode.kravId
        }

        val expectedResult = SisteAldersberegning2011()
        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } returns expectedResult
        }
        val kravService = mockk<KravService>()

        val sisteBeregningCreator = SisteBeregningCreator(
            kravService,
            creator2011,
            mockk(),
            mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        // kravService.fetchKravhode should not be called because kravIds match
        verify(exactly = 0) { kravService.fetchKravhode(any()) }
    }

    test("opprettSisteBeregning should fetch kravhode from service when kravId differs") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ).apply {
            kravId = 123L
        }
        val forrigeKravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ).apply {
            kravId = 456L
        }
        val beregningsresultat = createBeregningsresultat().apply {
            kravId = 456L // Different from kravhode.kravId
        }

        val expectedResult = SisteAldersberegning2011()
        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } returns expectedResult
        }
        val kravService = mockk<KravService> {
            every { fetchKravhode(456L) } returns forrigeKravhode
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            kravService,
            creator2011,
            mockk(),
            mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        // kravService.fetchKravhode should be called with beregningsresultat.kravId
        verify(exactly = 1) { kravService.fetchKravhode(456L) }
    }

    // ===========================================
    // Tests for vedtak filtering
    // ===========================================

    test("opprettSisteBeregning should filter vedtak to only innvilget") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        val innvilgetVedtak = createVedtak(VedtakResultatEnum.INNV)
        val avslaattVedtak = createVedtak(VedtakResultatEnum.AVSL)

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(innvilgetVedtak, avslaattVedtak),
            beregningResultat = beregningsresultat
        )

        // Only innvilget vedtak should be in the filtered list
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 1
        capturedSpec!!.filtrertVilkarsvedtakList[0].vilkarsvedtakResultatEnum shouldBe VedtakResultatEnum.INNV
    }

    test("opprettSisteBeregning should filter vedtak to only vilkarsprovd or ferdig status") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        val vedtakVilkarsprovd = createVedtak(VedtakResultatEnum.INNV, KravlinjeStatus.VILKARSPROVD)
        val vedtakFerdig = createVedtak(VedtakResultatEnum.INNV, KravlinjeStatus.FERDIG)
        val vedtakIkkeBehandlet = createVedtak(VedtakResultatEnum.INNV, KravlinjeStatus.TIL_BEHANDLING)

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(vedtakVilkarsprovd, vedtakFerdig, vedtakIkkeBehandlet),
            beregningResultat = beregningsresultat
        )

        // Only vedtak with VILKARSPROVD or FERDIG status should be in the filtered list
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 2
    }

    test("opprettSisteBeregning should filter vedtak to only Norwegian kravlinje") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        val norskVedtak = createVedtak(VedtakResultatEnum.INNV, KravlinjeStatus.VILKARSPROVD, LandkodeEnum.NOR)
        val utenlandskVedtak = createVedtak(VedtakResultatEnum.INNV, KravlinjeStatus.VILKARSPROVD, LandkodeEnum.SWE)

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(norskVedtak, utenlandskVedtak),
            beregningResultat = beregningsresultat
        )

        // Only Norwegian vedtak should be in the filtered list
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 1
        capturedSpec!!.filtrertVilkarsvedtakList[0].kravlinje?.land shouldBe LandkodeEnum.NOR
    }

    test("opprettSisteBeregning should filter vedtak by virkFom before beregningsresultat virkFom") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat().apply {
            virkFom = dateAtNoon(2025, Calendar.JUNE, 1)
        }

        val vedtakBefore = createVedtak(VedtakResultatEnum.INNV).apply {
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1) // Before beregningsresultat.virkFom
        }
        val vedtakAfter = createVedtak(VedtakResultatEnum.INNV).apply {
            virkFom = dateAtNoon(2025, Calendar.DECEMBER, 1) // After beregningsresultat.virkFom
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(vedtakBefore, vedtakAfter),
            beregningResultat = beregningsresultat
        )

        // Only vedtak with virkFom before beregningsresultat.virkFom should pass
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 1
    }

    test("opprettSisteBeregning should filter vedtak by virkTom after beregningsresultat virkTom") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat().apply {
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            virkTom = dateAtNoon(2025, Calendar.JUNE, 1)
        }

        val vedtakTomNull = createVedtak(VedtakResultatEnum.INNV).apply {
            virkFom = dateAtNoon(2024, Calendar.JANUARY, 1)
            virkTom = null // null is considered "after"
        }
        val vedtakTomAfter = createVedtak(VedtakResultatEnum.INNV).apply {
            virkFom = dateAtNoon(2024, Calendar.JANUARY, 1)
            virkTom = dateAtNoon(2025, Calendar.DECEMBER, 1) // After beregningsresultat.virkTom
        }
        val vedtakTomBefore = createVedtak(VedtakResultatEnum.INNV).apply {
            virkFom = dateAtNoon(2024, Calendar.JANUARY, 1)
            virkTom = dateAtNoon(2025, Calendar.MARCH, 1) // Before beregningsresultat.virkTom
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(vedtakTomNull, vedtakTomAfter, vedtakTomBefore),
            beregningResultat = beregningsresultat
        )

        // Only vedtak with virkTom after (or equal to) beregningsresultat.virkTom or null should pass
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 2
    }

    // ===========================================
    // Tests for spec values passed to creator
    // ===========================================

    test("opprettSisteBeregning should set regelverkKodePaNyttKrav from kravhode") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        capturedSpec shouldNotBe null
        capturedSpec!!.regelverkKodePaNyttKrav shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
    }

    test("opprettSisteBeregning should set fomDato and tomDato from beregningsresultat") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val fomDate = dateAtNoon(2025, Calendar.JANUARY, 1)
        val tomDate = dateAtNoon(2025, Calendar.DECEMBER, 31)
        val beregningsresultat = createBeregningsresultat().apply {
            virkFom = fomDate
            virkTom = tomDate
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        capturedSpec shouldNotBe null
        capturedSpec!!.fomDato shouldNotBe null
        capturedSpec!!.tomDato shouldNotBe null
    }

    test("opprettSisteBeregning should pass beregningsresultat to creator") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat()

        var capturedBeregningsresultat: AbstraktBeregningsResultat? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedBeregningsresultat = arg(1)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = emptyList(),
            beregningResultat = beregningsresultat
        )

        capturedBeregningsresultat shouldBe beregningsresultat
    }

    // ===========================================
    // Tests for isVirkTomAfterDate edge cases
    // ===========================================

    test("opprettSisteBeregning should include vedtak when both virkTom and beregningsresultat.virkTom are null") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat().apply {
            virkTom = null
        }

        val vedtakWithNullVirkTom = createVedtak(VedtakResultatEnum.INNV).apply {
            virkTom = null
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(vedtakWithNullVirkTom),
            beregningResultat = beregningsresultat
        )

        // When both are null, vedtak should be included
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 1
    }

    test("opprettSisteBeregning should include vedtak when vedtak virkTom is null and beregningsresultat.virkTom is set") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat().apply {
            virkTom = dateAtNoon(2025, Calendar.JUNE, 1)
        }

        val vedtakWithNullVirkTom = createVedtak(VedtakResultatEnum.INNV).apply {
            virkTom = null
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(vedtakWithNullVirkTom),
            beregningResultat = beregningsresultat
        )

        // When vedtak.virkTom is null, it's considered "after" any date
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 1
    }

    test("opprettSisteBeregning should exclude vedtak when vedtak virkTom is set and beregningsresultat.virkTom is null") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat().apply {
            virkTom = null
        }

        val vedtakWithVirkTom = createVedtak(VedtakResultatEnum.INNV).apply {
            virkTom = dateAtNoon(2025, Calendar.JUNE, 1)
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(vedtakWithVirkTom),
            beregningResultat = beregningsresultat
        )

        // When vedtak.virkTom is set but beregningsresultat.virkTom is null, vedtak is excluded
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 0
    }

    // ===========================================
    // Tests for complex filtering scenarios
    // ===========================================

    test("opprettSisteBeregning should filter vedtak by all criteria combined") {
        val kravhode = createKravhodeWithSoker(RegelverkTypeEnum.N_REG_G_OPPTJ)
        val beregningsresultat = createBeregningsresultat().apply {
            virkFom = dateAtNoon(2025, Calendar.JUNE, 1)
            virkTom = dateAtNoon(2025, Calendar.DECEMBER, 31)
        }

        // This vedtak passes all filters
        val validVedtak = VilkarsVedtak().apply {
            vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
            kravlinje = Kravlinje().apply {
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            virkTom = null
        }

        // This vedtak fails: not innvilget
        val failsInnvilget = VilkarsVedtak().apply {
            vilkarsvedtakResultatEnum = VedtakResultatEnum.AVSL
            kravlinje = Kravlinje().apply {
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            virkTom = null
        }

        // This vedtak fails: wrong status
        val failsStatus = VilkarsVedtak().apply {
            vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
            kravlinje = Kravlinje().apply {
                kravlinjeStatus = KravlinjeStatus.TIL_BEHANDLING
                land = LandkodeEnum.NOR
            }
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            virkTom = null
        }

        // This vedtak fails: not Norwegian
        val failsLand = VilkarsVedtak().apply {
            vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
            kravlinje = Kravlinje().apply {
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.SWE
            }
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            virkTom = null
        }

        // This vedtak fails: virkFom after beregningsresultat.virkFom
        val failsVirkFom = VilkarsVedtak().apply {
            vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
            kravlinje = Kravlinje().apply {
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }
            virkFom = dateAtNoon(2025, Calendar.JULY, 1) // After beregningsresultat.virkFom
            virkTom = null
        }

        var capturedSpec: SisteBeregningSpec? = null

        val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
            every { createBeregning(any(), any()) } answers {
                capturedSpec = arg(0)
                SisteAldersberegning2011()
            }
        }

        val sisteBeregningCreator = SisteBeregningCreator(
            mockk(), creator2011, mockk(), mockk()
        )

        sisteBeregningCreator.opprettSisteBeregning(
            kravhode = kravhode,
            vedtakListe = listOf(validVedtak, failsInnvilget, failsStatus, failsLand, failsVirkFom),
            beregningResultat = beregningsresultat
        )

        // Only the valid vedtak should pass all filters
        capturedSpec shouldNotBe null
        capturedSpec!!.filtrertVilkarsvedtakList.size shouldBe 1
        capturedSpec!!.filtrertVilkarsvedtakList.first() shouldBe validVedtak
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun createSisteBeregningCreator(): SisteBeregningCreator {
    val kravService = mockk<KravService>()
    val creator2011 = mockk<Alderspensjon2011SisteBeregningCreator> {
        every { createBeregning(any(), any()) } returns SisteAldersberegning2011()
    }
    val creator2016 = mockk<Alderspensjon2016SisteBeregningCreator> {
        every { createBeregning(any(), any()) } returns SisteAldersberegning2016()
    }
    val creator2025 = mockk<Alderspensjon2025SisteBeregningCreator> {
        every { createBeregning(any(), any()) } returns SisteAldersberegning2011()
    }

    return SisteBeregningCreator(kravService, creator2011, creator2016, creator2025)
}

private fun createKravhodeWithSoker(regelverkType: RegelverkTypeEnum? = RegelverkTypeEnum.N_REG_G_OPPTJ): Kravhode =
    Kravhode().apply {
        regelverkTypeEnum = regelverkType
        persongrunnlagListe = mutableListOf(
            Persongrunnlag().apply {
                fodselsdato = dateAtNoon(1960, Calendar.JANUARY, 1)
                penPerson = PenPerson().apply { penPersonId = 1L }
                personDetaljListe = mutableListOf(
                    PersonDetalj().apply {
                        bruk = true
                        grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                        virkFom = dateAtNoon(2020, Calendar.JANUARY, 1)
                    }
                )
            }
        )
        kravlinjeListe = mutableListOf(
            Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
        )
    }

private fun createBeregningsresultat(): BeregningsResultatAlderspensjon2011 =
    BeregningsResultatAlderspensjon2011().apply {
        virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
        beregningKapittel19 = AldersberegningKapittel19().apply {
            pensjonUnderUtbetaling = PensjonUnderUtbetaling()
        }
    }

private fun createVedtak(
    resultat: VedtakResultatEnum,
    status: KravlinjeStatus = KravlinjeStatus.VILKARSPROVD,
    land: LandkodeEnum = LandkodeEnum.NOR
): VilkarsVedtak =
    VilkarsVedtak().apply {
        vilkarsvedtakResultatEnum = resultat
        kravlinje = Kravlinje().apply {
            kravlinjeStatus = status
            this.land = land
        }
        virkFom = dateAtNoon(2024, Calendar.JANUARY, 1)
        virkTom = null
    }
