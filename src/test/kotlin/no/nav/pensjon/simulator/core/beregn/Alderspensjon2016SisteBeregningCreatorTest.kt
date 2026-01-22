package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class Alderspensjon2016SisteBeregningCreatorTest : FunSpec({

    // --- Tests for ytelseskomponenter filtering ---

    test("createBeregning should filter out irrelevante ytelseskomponenter from 2011 result") {
        val beregning = createBeregning(
            ytelseskomponentListe2011 = mutableListOf(
                Garantipensjon().apply { brukt = true; opphort = false; brutto = 1 },
                Skjermingstillegg().apply { brukt = true; opphort = true; brutto = 2 }, // irrelevant
                Inntektspensjon().apply { brukt = false; opphort = false; brutto = 3 }, // irrelevant
                Garantitillegg().apply { brukt = true; opphort = false; brutto = 4 }
            )
        )

        with(beregning.pensjonUnderUtbetaling2011!!) {
            ytelseskomponenter.size shouldBe 2
            ytelseskomponenter[0].brutto shouldBe 1
            ytelseskomponenter[1].brutto shouldBe 4
        }
    }

    test("createBeregning should filter out irrelevante ytelseskomponenter from 2025 result") {
        val beregning = createBeregning(
            ytelseskomponentListe2025 = mutableListOf(
                Inntektspensjon().apply { brukt = true; opphort = false; brutto = 10 },
                Garantipensjon().apply { brukt = true; opphort = true; brutto = 20 }, // irrelevant
                Garantitillegg().apply { brukt = true; opphort = false; brutto = 30 }
            )
        )

        with(beregning.pensjonUnderUtbetaling2025!!) {
            ytelseskomponenter.size shouldBe 2
            ytelseskomponenter[0].brutto shouldBe 10
            ytelseskomponenter[1].brutto shouldBe 30
        }
    }

    test("createBeregning should filter pensjonUnderUtbetaling2011UtenGJR") {
        val beregning = createBeregning(
            pensjonUnderUtbetalingUtenGJR = PensjonUnderUtbetaling().apply {
                ytelseskomponenter = mutableListOf(
                    Garantipensjon().apply { brukt = true; opphort = false; brutto = 100 },
                    Inntektspensjon().apply { brukt = false; opphort = false; brutto = 200 } // irrelevant
                )
            }
        )

        with(beregning.pensjonUnderUtbetaling2011UtenGJR!!) {
            ytelseskomponenter.size shouldBe 1
            ytelseskomponenter[0].brutto shouldBe 100
        }
    }

    test("createBeregning should filter top-level pensjonUnderUtbetaling") {
        val beregning = createBeregning(
            topLevelPensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                ytelseskomponenter = mutableListOf(
                    Garantipensjon().apply { brukt = true; opphort = false; brutto = 500 },
                    Inntektspensjon().apply { brukt = true; opphort = true; brutto = 600 } // irrelevant
                )
            }
        )

        with(beregning.pensjonUnderUtbetaling!!) {
            ytelseskomponenter.size shouldBe 1
            ytelseskomponenter[0].brutto shouldBe 500
        }
    }

    // --- Tests for regelverkTypeEnum ---

    test("createBeregning should set regelverkTypeEnum from spec") {
        val beregning = createBeregning(regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ)
        beregning.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
    }

    // --- Tests for virkDato ---

    test("createBeregning should set virkDato from beregningsresultat") {
        val virkFom = dateAtNoon(2025, Calendar.MARCH, 1)
        val beregning = createBeregning(virkFom = virkFom)
        beregning.virkDato shouldBe virkFom
    }

    // --- Tests for kapittel 19 data ---

    test("createBeregning should copy tt_anv from beregningKapittel19") {
        val beregning = createBeregning(tt_anv_kapittel19 = 40)
        beregning.tt_anv shouldBe 40
    }

    test("createBeregning should copy restpensjon from beregningKapittel19") {
        val beregning = createBeregning(
            restpensjon = Basispensjon().apply { totalbelop = 5000.0 }
        )
        beregning.restpensjon shouldNotBe null
        beregning.restpensjon?.totalbelop shouldBe 5000.0
    }

    test("createBeregning should copy basispensjon from beregningKapittel19") {
        val beregning = createBeregning(
            basispensjon = Basispensjon().apply { totalbelop = 7500.0 }
        )
        beregning.basispensjon shouldNotBe null
        beregning.basispensjon?.totalbelop shouldBe 7500.0
    }

    test("createBeregning should copy restpensjonUtenGJR from beregningKapittel19") {
        val beregning = createBeregning(
            restpensjonUtenGJR = Basispensjon().apply { totalbelop = 6000.0 }
        )
        beregning.restpensjonUtenGJR shouldNotBe null
        beregning.restpensjonUtenGJR?.totalbelop shouldBe 6000.0
    }

    test("createBeregning should copy basispensjonUtenGJR from beregningKapittel19") {
        val beregning = createBeregning(
            basispensjonUtenGJR = Basispensjon().apply { totalbelop = 8000.0 }
        )
        beregning.basispensjonUtenGJR shouldNotBe null
        beregning.basispensjonUtenGJR?.totalbelop shouldBe 8000.0
    }

    // --- Tests for kapittel 20 data ---

    test("createBeregning should set beregningsMetodeEnum from beregningKapittel20") {
        val beregning = createBeregning(beregningsMetode = BeregningsmetodeEnum.FOLKETRYGD)
        beregning.beregningsMetodeEnum shouldBe BeregningsmetodeEnum.FOLKETRYGD
    }

    test("createBeregning should set prorataBrok_kap_20 from beregningKapittel20") {
        val brok = Brok().apply { teller = 30; nevner = 40 }
        val beregning = createBeregning(prorataBrok = brok)
        beregning.prorataBrok_kap_20?.teller shouldBe 30
        beregning.prorataBrok_kap_20?.nevner shouldBe 40
    }

    test("createBeregning should set tt_anv_kap_20 from beregningKapittel20") {
        val beregning = createBeregning(tt_anv_kapittel20 = 35)
        beregning.tt_anv_kap_20 shouldBe 35
    }

    test("createBeregning should set beholdninger from beregningKapittel20") {
        val beholdninger = Beholdninger()
        val beregning = createBeregning(beholdninger = beholdninger)
        beregning.beholdninger shouldBe beholdninger
    }

    test("createBeregning should prefer resultatTypeEnum from kapittel19 over kapittel20") {
        val beregning = createBeregning(
            resultatTypeKapittel19 = ResultattypeEnum.AP,
            resultatTypeKapittel20 = ResultattypeEnum.AFP
        )
        beregning.resultatTypeEnum shouldBe ResultattypeEnum.AP
    }

    test("createBeregning should fallback to resultatTypeEnum from kapittel20 when kapittel19 is null") {
        val beregning = createBeregning(
            resultatTypeKapittel19 = null,
            resultatTypeKapittel20 = ResultattypeEnum.AFP,
            includeKapittel19 = false
        )
        beregning.resultatTypeEnum shouldBe ResultattypeEnum.AFP
    }

    // --- Tests for benyttetSivilstand ---

    test("createBeregning should set benyttetSivilstandEnum from resultat2011") {
        val beregning = createBeregning(benyttetSivilstand2011 = BorMedTypeEnum.J_EKTEF)
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
    }

    test("createBeregning should fallback to benyttetSivilstandEnum from resultat2025") {
        val beregning = createBeregning(
            benyttetSivilstand2011 = null,
            benyttetSivilstand2025 = BorMedTypeEnum.SAMBOER1_5,
            includeKapittel19 = false
        )
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.SAMBOER1_5
    }

    // --- Tests for beregningsInformasjon ---

    test("createBeregning should set epsMottarPensjon from beregningsInformasjonKapittel19") {
        val beregning = createBeregning(epsMottarPensjon19 = true)
        beregning.epsMottarPensjon shouldBe true
    }

    test("createBeregning should fallback to epsMottarPensjon from beregningsInformasjonKapittel20") {
        val beregning = createBeregning(
            epsMottarPensjon19 = null,
            epsMottarPensjon20 = true,
            includeKapittel19 = false
        )
        beregning.epsMottarPensjon shouldBe true
    }

    test("createBeregning should set gjenlevenderettAnvendt from beregningsInformasjonKapittel19") {
        val beregning = createBeregning(gjenlevenderettAnvendt19 = true)
        beregning.gjenlevenderettAnvendt shouldBe true
    }

    test("createBeregning should fallback to gjenlevenderettAnvendt from beregningsInformasjonKapittel20") {
        val beregning = createBeregning(
            gjenlevenderettAnvendt19 = null,
            gjenlevenderettAnvendt20 = true,
            includeKapittel19 = false
        )
        beregning.gjenlevenderettAnvendt shouldBe true
    }

    // --- Tests for anvendtGjenlevenderettVedtak ---

    test("createBeregning should set anvendtGjenlevenderettVedtak from filtrertVilkarsvedtakList") {
        val gjrVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
        val beregning = createBeregning(
            filtrertVilkarsvedtakList = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                gjrVedtak,
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.ET }
            )
        )
        beregning.anvendtGjenlevenderettVedtak shouldBe gjrVedtak
    }

    test("createBeregning should not set anvendtGjenlevenderettVedtak when no GJR vedtak present") {
        val beregning = createBeregning(
            filtrertVilkarsvedtakList = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            )
        )
        beregning.anvendtGjenlevenderettVedtak shouldBe null
    }

    // --- Tests for alternativ konvensjon data (tapende delberegning) ---

    test("createBeregning should set alternativ konvensjon data from tapende delberegning") {
        val tapendeYtelseskomponenter = mutableListOf<Ytelseskomponent>(
            Garantipensjon().apply { brukt = true; opphort = false; brutto = 999 }
        )
        val tapendeBeholdninger = Beholdninger()
        val tapendeBrok = Brok().apply { teller = 10; nevner = 20 }

        val beregning = createBeregning(
            beregningsMetode = BeregningsmetodeEnum.FOLKETRYGD,
            tapendeDelberegning = AldersberegningKapittel20().apply {
                beregningsMetodeEnum = BeregningsmetodeEnum.EOS // different from winning
                pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                    ytelseskomponenter = tapendeYtelseskomponenter
                }
                beholdninger = tapendeBeholdninger
                prorataBrok = tapendeBrok
                tt_anv = 25
            }
        )

        beregning.pensjonUnderUtbetaling2025AltKonv shouldNotBe null
        beregning.pensjonUnderUtbetaling2025AltKonv?.ytelseskomponenter?.size shouldBe 1
        beregning.pensjonUnderUtbetaling2025AltKonv?.ytelseskomponenter?.get(0)?.brutto shouldBe 999
        beregning.beholdningerAltKonv shouldBe tapendeBeholdninger
        beregning.prorataBrok_kap_20AltKonv?.teller shouldBe 10
        beregning.prorataBrok_kap_20AltKonv?.nevner shouldBe 20
        beregning.tt_anv_kap_20AltKonv shouldBe 25
    }

    // --- Tests from base class populate() ---

    test("createBeregning should set sivilstandTypeEnum from forrigeKravhode søker detalj") {
        val beregning = createBeregning(
            forrigeKravhode = kravhodeWithSoker(SivilstandEnum.GIFT)
        )
        beregning.sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
    }

    test("createBeregning should set vilkarsvedtakEktefelletillegg from filtrertVilkarsvedtakList") {
        val etVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.ET }
        val beregning = createBeregning(
            filtrertVilkarsvedtakList = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                etVedtak
            ),
            includeKapittel19 = false // to avoid GJR vedtak logic
        )
        beregning.vilkarsvedtakEktefelletillegg shouldBe etVedtak
    }

    test("createBeregning should set avdodesPersongrunnlag from forrigeKravhode") {
        val avdodGrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = 99L }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                }
            )
        }
        val beregning = createBeregning(
            forrigeKravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(avdodGrunnlag)
            }
        )
        beregning.avdodesPersongrunnlag shouldBe avdodGrunnlag
    }

    test("createBeregning should set eps from forrigeKravhode") {
        val epsGrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = 88L }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                }
            )
        }
        val beregning = createBeregning(
            forrigeKravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(epsGrunnlag)
            }
        )
        beregning.eps shouldNotBe null
    }
})

private fun createBeregning(
    ytelseskomponentListe2011: MutableList<Ytelseskomponent> = mutableListOf(),
    ytelseskomponentListe2025: MutableList<Ytelseskomponent> = mutableListOf(),
    regelverkType: RegelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ,
    virkFom: Date? = null,
    tt_anv_kapittel19: Int? = null,
    tt_anv_kapittel20: Int? = null,
    restpensjon: Basispensjon? = null,
    basispensjon: Basispensjon? = null,
    restpensjonUtenGJR: Basispensjon? = null,
    basispensjonUtenGJR: Basispensjon? = null,
    beregningsMetode: BeregningsmetodeEnum? = null,
    prorataBrok: Brok? = null,
    beholdninger: Beholdninger? = null,
    resultatTypeKapittel19: ResultattypeEnum? = null,
    resultatTypeKapittel20: ResultattypeEnum? = null,
    benyttetSivilstand2011: BorMedTypeEnum? = null,
    benyttetSivilstand2025: BorMedTypeEnum? = null,
    epsMottarPensjon19: Boolean? = null,
    epsMottarPensjon20: Boolean? = null,
    gjenlevenderettAnvendt19: Boolean? = null,
    gjenlevenderettAnvendt20: Boolean? = null,
    pensjonUnderUtbetalingUtenGJR: PensjonUnderUtbetaling? = null,
    topLevelPensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
    filtrertVilkarsvedtakList: List<VilkarsVedtak> = emptyList(),
    tapendeDelberegning: AldersberegningKapittel20? = null,
    forrigeKravhode: Kravhode? = null,
    includeKapittel19: Boolean = true
): SisteAldersberegning2016 {
    val beregningsResultat2011 = if (includeKapittel19 || ytelseskomponentListe2011.isNotEmpty() ||
        pensjonUnderUtbetalingUtenGJR != null || tt_anv_kapittel19 != null ||
        restpensjon != null || basispensjon != null || restpensjonUtenGJR != null ||
        basispensjonUtenGJR != null || resultatTypeKapittel19 != null ||
        benyttetSivilstand2011 != null || epsMottarPensjon19 != null || gjenlevenderettAnvendt19 != null
    ) {
        BeregningsResultatAlderspensjon2011().apply {
            if (ytelseskomponentListe2011.isNotEmpty()) {
                pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                    ytelseskomponenter = ytelseskomponentListe2011
                }
            }
            this.pensjonUnderUtbetalingUtenGJR = pensjonUnderUtbetalingUtenGJR
            benyttetSivilstandEnum = benyttetSivilstand2011

            beregningKapittel19 = AldersberegningKapittel19().apply {
                tt_anv = tt_anv_kapittel19 ?: 0
                this.restpensjon = restpensjon
                this.basispensjon = basispensjon
                this.restpensjonUtenGJR = restpensjonUtenGJR
                this.basispensjonUtenGJR = basispensjonUtenGJR
                resultatTypeEnum = resultatTypeKapittel19
            }

            beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
                epsMottarPensjon = epsMottarPensjon19 ?: false
                gjenlevenderettAnvendt = gjenlevenderettAnvendt19 ?: false
            }
        }
    } else null

    val beregningsResultat2025 = BeregningsResultatAlderspensjon2025().apply {
        if (ytelseskomponentListe2025.isNotEmpty()) {
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                ytelseskomponenter = ytelseskomponentListe2025
            }
        }
        benyttetSivilstandEnum = benyttetSivilstand2025

        beregningKapittel20 = AldersberegningKapittel20().apply {
            beregningsMetodeEnum = beregningsMetode
            this.prorataBrok = prorataBrok
            tt_anv = tt_anv_kapittel20 ?: 0
            this.beholdninger = beholdninger
            resultatTypeEnum = resultatTypeKapittel20

            if (tapendeDelberegning != null) {
                delberegning2011Liste = mutableListOf(
                    BeregningRelasjon().apply {
                        beregning2011 = tapendeDelberegning
                    }
                )
            }
        }

        beregningsInformasjonKapittel20 = BeregningsInformasjon().apply {
            epsMottarPensjon = epsMottarPensjon20 ?: false
            gjenlevenderettAnvendt = gjenlevenderettAnvendt20 ?: false
        }
    }

    val spec = SisteBeregningSpec(
        beregningsresultat = BeregningsResultatAlderspensjon2016().apply {
            this.virkFom = virkFom
            this.beregningsResultat2011 = beregningsResultat2011
            this.beregningsResultat2025 = beregningsResultat2025
            this.pensjonUnderUtbetaling = topLevelPensjonUnderUtbetaling
        },
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

    return Alderspensjon2016SisteBeregningCreator(kravService = mockk()).createBeregning(
        spec,
        BeregningsResultatAlderspensjon2016()
    ) as SisteAldersberegning2016
}

private fun kravhodeWithSoker(sivilstand: SivilstandEnum): Kravhode =
    Kravhode().apply {
        persongrunnlagListe = mutableListOf(
            Persongrunnlag().apply {
                penPerson = PenPerson().apply { penPersonId = 1L }
                personDetaljListe = mutableListOf(
                    PersonDetalj().apply {
                        bruk = true
                        grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                        sivilstandTypeEnum = sivilstand
                    }
                )
            }
        )
    }
