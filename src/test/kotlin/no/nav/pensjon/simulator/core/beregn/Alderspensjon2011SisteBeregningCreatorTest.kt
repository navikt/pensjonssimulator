package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.AfpTillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Familietillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class Alderspensjon2011SisteBeregningCreatorTest : FunSpec({

    /**
     * Test filter for irrelevante ytelseskomponenter.
     * En ytelseskomponent er irrelevant hvis den er opphørt eller ubrukt.
     */

    test("createBeregning should filter out irrelevante ytelseskomponenter") {
        val beregning = createBeregning(
            ytelseskomponentListe = mutableListOf(
                Garantipensjon().apply {
                    brukt = true
                    opphort = false
                    brutto = 1
                },
                AfpTillegg().apply {
                    brukt = true
                    opphort = true // => irrelevant
                    brutto = 2
                },
                Familietillegg().apply {
                    brukt = false // => irrelevant
                    opphort = false
                    brutto = 3
                },
                Skjermingstillegg().apply {
                    brukt = true
                    opphort = false
                    brutto = 4
                })
        )

        with(beregning.pensjonUnderUtbetaling!!) {
            ytelseskomponenter.size shouldBe 2
            ytelseskomponenter[0].brutto shouldBe 1
            ytelseskomponenter[1].brutto shouldBe 4
        }
    }

    test("createBeregning should keep all ytelseskomponenter when all are relevant") {
        val beregning = createBeregning(
            ytelseskomponentListe = mutableListOf(
                Garantipensjon().apply { brukt = true; opphort = false; brutto = 100 },
                Skjermingstillegg().apply { brukt = true; opphort = false; brutto = 200 }
            )
        )

        beregning.pensjonUnderUtbetaling!!.ytelseskomponenter.size shouldBe 2
    }

    // ===========================================
    // Tests for regelverkTypeEnum
    // ===========================================

    test("createBeregning should set regelverkTypeEnum from spec") {
        val beregning = createBeregning(regelverkType = RegelverkTypeEnum.N_REG_G_OPPTJ)
        beregning.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
    }

    test("createBeregning should handle null regelverkTypeEnum") {
        val beregning = createBeregning(regelverkType = null)
        beregning.regelverkTypeEnum shouldBe null
    }

    // ===========================================
    // Tests for virkDato
    // ===========================================

    test("createBeregning should set virkDato from beregningsresultat virkFom") {
        val virkFom = dateAtNoon(2024, Calendar.MARCH, 1)
        val beregning = createBeregning(virkFom = virkFom)
        beregning.virkDato shouldBe virkFom
    }

    test("createBeregning should not set virkDato when virkFom is null") {
        val beregning = createBeregning(virkFom = null)
        beregning.virkDato shouldBe null
    }

    // ===========================================
    // Tests for benyttetSivilstandEnum
    // ===========================================

    test("createBeregning should set benyttetSivilstandEnum from beregningsresultat") {
        val beregning = createBeregning(benyttetSivilstand = BorMedTypeEnum.J_EKTEF)
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
    }

    test("createBeregning should handle null benyttetSivilstandEnum") {
        val beregning = createBeregning(benyttetSivilstand = null)
        beregning.benyttetSivilstandEnum shouldBe null
    }

    // ===========================================
    // Tests for Kapittel 19 data
    // ===========================================

    test("createBeregning should set tt_anv from beregningKapittel19") {
        val beregning = createBeregning(kap19TtAnv = 40)
        beregning.tt_anv shouldBe 40
    }

    test("createBeregning should set resultatTypeEnum from beregningKapittel19") {
        val beregning = createBeregning(kap19ResultatType = ResultattypeEnum.AP2011)
        beregning.resultatTypeEnum shouldBe ResultattypeEnum.AP2011
    }

    test("createBeregning should copy basispensjon from beregningKapittel19") {
        val basispensjon = Basispensjon().apply { totalbelop = 150000.0 }
        val beregning = createBeregning(kap19Basispensjon = basispensjon)

        beregning.basispensjon shouldNotBe null
        beregning.basispensjon!!.totalbelop shouldBe 150000.0
    }

    test("createBeregning should copy restpensjon from beregningKapittel19") {
        val restpensjon = Basispensjon().apply { totalbelop = 50000.0 }
        val beregning = createBeregning(kap19Restpensjon = restpensjon)

        beregning.restpensjon shouldNotBe null
        beregning.restpensjon!!.totalbelop shouldBe 50000.0
    }

    test("createBeregning should handle null Kapittel 19 values") {
        val beregning = createBeregning(
            kap19TtAnv = null,
            kap19ResultatType = null,
            kap19Basispensjon = null,
            kap19Restpensjon = null
        )

        beregning.tt_anv shouldBe 0 // default value
        beregning.resultatTypeEnum shouldBe null
        beregning.basispensjon shouldBe null
        beregning.restpensjon shouldBe null
    }

    // ===========================================
    // Tests for BeregningsInformasjon
    // ===========================================

    test("createBeregning should set epsMottarPensjon true from beregningsInformasjonKapittel19") {
        val beregning = createBeregning(epsMottarPensjon = true)
        beregning.epsMottarPensjon shouldBe true
    }

    test("createBeregning should set epsMottarPensjon false from beregningsInformasjonKapittel19") {
        val beregning = createBeregning(epsMottarPensjon = false)
        beregning.epsMottarPensjon shouldBe false
    }

    test("createBeregning should set gjenlevenderettAnvendt true from beregningsInformasjonKapittel19") {
        val beregning = createBeregning(gjenlevenderettAnvendt = true)
        beregning.gjenlevenderettAnvendt shouldBe true
    }

    test("createBeregning should set gjenlevenderettAnvendt false from beregningsInformasjonKapittel19") {
        val beregning = createBeregning(gjenlevenderettAnvendt = false)
        beregning.gjenlevenderettAnvendt shouldBe false
    }

    // ===========================================
    // Tests for anvendtGjenlevenderettVedtak
    // ===========================================

    test("createBeregning should set anvendtGjenlevenderettVedtak from GJR vedtak in filtrertVilkarsvedtakList") {
        val gjrVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
        val beregning = createBeregning(filtrertVilkarsvedtakList = listOf(gjrVedtak))

        beregning.anvendtGjenlevenderettVedtak shouldBe gjrVedtak
    }

    test("createBeregning should not set anvendtGjenlevenderettVedtak when no GJR vedtak") {
        val apVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
        val beregning = createBeregning(filtrertVilkarsvedtakList = listOf(apVedtak))

        beregning.anvendtGjenlevenderettVedtak shouldBe null
    }

    test("createBeregning should find first GJR vedtak when multiple vedtak present") {
        val gjrVedtak1 = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
        val gjrVedtak2 = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
        val apVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
        val beregning = createBeregning(filtrertVilkarsvedtakList = listOf(apVedtak, gjrVedtak1, gjrVedtak2))

        beregning.anvendtGjenlevenderettVedtak shouldBe gjrVedtak1
    }

    // ===========================================
    // Tests for base class populate() - sivilstand
    // ===========================================

    test("createBeregning should set sivilstandTypeEnum from soker PersonDetalj") {
        val kravhode = createKravhodeWithSoker(sivilstand = SivilstandEnum.ENKE)
        val beregning = createBeregning(forrigeKravhode = kravhode)

        beregning.sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
    }

    test("createBeregning should not set sivilstandTypeEnum when soker detalj is null") {
        val beregning = createBeregning(forrigeKravhode = null)
        beregning.sivilstandTypeEnum shouldBe null
    }

    // ===========================================
    // Tests for base class populate() - vilkarsvedtakEktefelletillegg
    // ===========================================

    test("createBeregning should set vilkarsvedtakEktefelletillegg from ET vedtak") {
        val etVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.ET }
        val beregning = createBeregning(filtrertVilkarsvedtakList = listOf(etVedtak))

        beregning.vilkarsvedtakEktefelletillegg shouldBe etVedtak
    }

    test("createBeregning should not set vilkarsvedtakEktefelletillegg when no ET vedtak") {
        val apVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
        val beregning = createBeregning(filtrertVilkarsvedtakList = listOf(apVedtak))

        beregning.vilkarsvedtakEktefelletillegg shouldBe null
    }

    // ===========================================
    // Tests for base class populate() - avdodesPersongrunnlag
    // ===========================================

    test("createBeregning should set avdodesPersongrunnlag from kravhode") {
        val avdodGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD)
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(avdodGrunnlag)
        }
        val beregning = createBeregning(forrigeKravhode = kravhode)

        beregning.avdodesPersongrunnlag shouldNotBe null
    }

    test("createBeregning should not set avdodesPersongrunnlag when no avdod in kravhode") {
        val sokerGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER)
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(sokerGrunnlag)
        }
        val beregning = createBeregning(forrigeKravhode = kravhode)

        beregning.avdodesPersongrunnlag shouldBe null
    }

    // ===========================================
    // Tests for base class populate() - eps grunnlag
    // ===========================================

    test("createBeregning should set eps from kravhode when no gjenlevenderettighet") {
        val epsGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.EKTEF)
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(epsGrunnlag)
            kravlinjeListe = mutableListOf() // no GJR kravlinje
        }
        val beregning = createBeregning(forrigeKravhode = kravhode)

        beregning.eps shouldNotBe null
    }

    test("createBeregning should not set eps when no eps in kravhode") {
        val sokerGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER)
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(sokerGrunnlag)
        }
        val beregning = createBeregning(forrigeKravhode = kravhode)

        beregning.eps shouldBe null
    }

    // ===========================================
    // Tests for beregningsresultat type handling
    // ===========================================

    test("createBeregning should not populate from beregningsresultat when not BeregningsResultatAlderspensjon2011") {
        val beregning = Alderspensjon2011SisteBeregningCreator(kravService = mockk()).createBeregning(
            SisteBeregningSpec(
                beregningsresultat = null, // not a BeregningsResultatAlderspensjon2011
                regelverkKodePaNyttKrav = RegelverkTypeEnum.N_REG_G_OPPTJ,
                forrigeKravhode = null,
                filtrertVilkarsvedtakList = emptyList(),
                isRegelverk1967 = false,
                vilkarsvedtakListe = emptyList(),
                kravhode = null,
                beregning = null,
                fomDato = null,
                tomDato = null,
                regelverk1967VirkToEarly = false
            ),
            null
        ) as SisteAldersberegning2011

        beregning.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
        beregning.virkDato shouldBe null
        beregning.pensjonUnderUtbetaling shouldBe null
    }

    // ===========================================
    // Integration test
    // ===========================================

    test("createBeregning should populate all fields from complete spec") {
        val gjrVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
        val etVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.ET }
        val vedtakList = listOf(gjrVedtak, etVedtak)

        val epsGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.EKTEF)
        val avdodGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD)
        val sokerGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER, sivilstand = SivilstandEnum.ENKE)

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(sokerGrunnlag, epsGrunnlag, avdodGrunnlag)
        }

        val beregning = createBeregning(
            regelverkType = RegelverkTypeEnum.N_REG_G_OPPTJ,
            virkFom = dateAtNoon(2024, Calendar.JANUARY, 1),
            benyttetSivilstand = BorMedTypeEnum.J_EKTEF,
            kap19TtAnv = 35,
            kap19ResultatType = ResultattypeEnum.AP2011,
            kap19Basispensjon = Basispensjon().apply { totalbelop = 200000.0 },
            kap19Restpensjon = Basispensjon().apply { totalbelop = 75000.0 },
            epsMottarPensjon = true,
            gjenlevenderettAnvendt = true,
            filtrertVilkarsvedtakList = vedtakList,
            forrigeKravhode = kravhode,
            ytelseskomponentListe = mutableListOf(
                Garantipensjon().apply { brukt = true; opphort = false; brutto = 5000 }
            )
        )

        beregning.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
        beregning.virkDato shouldBe dateAtNoon(2024, Calendar.JANUARY, 1)
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
        beregning.tt_anv shouldBe 35
        beregning.resultatTypeEnum shouldBe ResultattypeEnum.AP2011
        beregning.basispensjon!!.totalbelop shouldBe 200000.0
        beregning.restpensjon!!.totalbelop shouldBe 75000.0
        beregning.epsMottarPensjon shouldBe true
        beregning.gjenlevenderettAnvendt shouldBe true
        beregning.anvendtGjenlevenderettVedtak shouldBe gjrVedtak
        beregning.vilkarsvedtakEktefelletillegg shouldBe etVedtak
        beregning.sivilstandTypeEnum shouldBe SivilstandEnum.ENKE
        beregning.avdodesPersongrunnlag shouldNotBe null
        beregning.eps shouldNotBe null
        beregning.pensjonUnderUtbetaling!!.ytelseskomponenter.size shouldBe 1
    }
})

// ===========================================
// Helper functions
// ===========================================

private fun createBeregning(
    regelverkType: RegelverkTypeEnum? = null,
    virkFom: Date? = null,
    benyttetSivilstand: BorMedTypeEnum? = null,
    kap19TtAnv: Int? = null,
    kap19ResultatType: ResultattypeEnum? = null,
    kap19Basispensjon: Basispensjon? = null,
    kap19Restpensjon: Basispensjon? = null,
    epsMottarPensjon: Boolean? = null,
    gjenlevenderettAnvendt: Boolean? = null,
    filtrertVilkarsvedtakList: List<VilkarsVedtak> = emptyList(),
    forrigeKravhode: Kravhode? = null,
    ytelseskomponentListe: MutableList<Ytelseskomponent> = mutableListOf()
): SisteAldersberegning2011 {
    val beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
        this.virkFom = virkFom
        this.benyttetSivilstandEnum = benyttetSivilstand
        this.pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
            ytelseskomponenter = ytelseskomponentListe
        }
        this.beregningKapittel19 = AldersberegningKapittel19().apply {
            kap19TtAnv?.let { this.tt_anv = it }
            kap19ResultatType?.let { this.resultatTypeEnum = it }
            this.basispensjon = kap19Basispensjon
            this.restpensjon = kap19Restpensjon
        }
        this.beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
            epsMottarPensjon?.let { this.epsMottarPensjon = it }
            gjenlevenderettAnvendt?.let { this.gjenlevenderettAnvendt = it }
        }
    }

    val spec = SisteBeregningSpec(
        beregningsresultat = beregningsresultat,
        regelverkKodePaNyttKrav = regelverkType,
        forrigeKravhode = forrigeKravhode,
        filtrertVilkarsvedtakList = filtrertVilkarsvedtakList,
        isRegelverk1967 = false,
        vilkarsvedtakListe = emptyList(),
        kravhode = null,
        beregning = null,
        fomDato = null,
        tomDato = null,
        regelverk1967VirkToEarly = false
    )

    return Alderspensjon2011SisteBeregningCreator(kravService = mockk()).createBeregning(
        spec,
        beregningsresultat
    ) as SisteAldersberegning2011
}

private fun createKravhodeWithSoker(sivilstand: SivilstandEnum? = null): Kravhode {
    val sokerGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER, sivilstand)
    return Kravhode().apply {
        persongrunnlagListe = mutableListOf(sokerGrunnlag)
    }
}

private fun createPersongrunnlag(
    rolle: GrunnlagsrolleEnum,
    sivilstand: SivilstandEnum? = null
): Persongrunnlag =
    Persongrunnlag().apply {
        fodselsdato = dateAtNoon(1960, Calendar.JANUARY, 1)
        penPerson = PenPerson().apply { penPersonId = 1L }
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = rolle
                virkFom = dateAtNoon(2020, Calendar.JANUARY, 1)
                sivilstand?.let { sivilstandTypeEnum = it }
            }
        )
    }
