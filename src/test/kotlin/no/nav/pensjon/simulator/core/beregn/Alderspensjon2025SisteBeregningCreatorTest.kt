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

class Alderspensjon2025SisteBeregningCreatorTest : FunSpec({

    // --- Tests for ytelseskomponenter filtering ---

    test("createBeregning should filter out irrelevante ytelseskomponenter") {
        val beregning = createBeregning(
            ytelseskomponentListe = mutableListOf(
                Inntektspensjon().apply { brukt = true; opphort = false; brutto = 100 },
                Garantipensjon().apply { brukt = true; opphort = true; brutto = 200 }, // irrelevant - opphort
                Garantitillegg().apply { brukt = false; opphort = false; brutto = 300 }, // irrelevant - not brukt
                Skjermingstillegg().apply { brukt = true; opphort = false; brutto = 400 }
            )
        )

        with(beregning.pensjonUnderUtbetaling!!) {
            ytelseskomponenter.size shouldBe 2
            ytelseskomponenter[0].brutto shouldBe 100
            ytelseskomponenter[1].brutto shouldBe 400
        }
    }

    test("createBeregning should handle empty ytelseskomponenter list") {
        val beregning = createBeregning(
            ytelseskomponentListe = mutableListOf(
                Garantipensjon().apply { brukt = true; opphort = true; brutto = 100 } // all irrelevant
            )
        )

        beregning.pensjonUnderUtbetaling?.ytelseskomponenter?.size shouldBe 0
    }

    // --- Tests for regelverkTypeEnum ---

    test("createBeregning should set regelverkTypeEnum from spec") {
        val beregning = createBeregning(regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ)
        beregning.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
    }

    test("createBeregning should handle null regelverkTypeEnum") {
        val beregning = createBeregning(regelverkType = null)
        beregning.regelverkTypeEnum shouldBe null
    }

    // --- Tests for virkDato ---

    test("createBeregning should set virkDato from beregningsresultat") {
        val virkFom = dateAtNoon(2025, Calendar.MARCH, 1)
        val beregning = createBeregning(virkFom = virkFom)
        beregning.virkDato shouldBe virkFom
    }

    test("createBeregning should handle null virkFom") {
        val beregning = createBeregning(virkFom = null)
        beregning.virkDato shouldBe null
    }

    // --- Tests for benyttetSivilstand ---

    test("createBeregning should set benyttetSivilstandEnum from beregningsresultat") {
        val beregning = createBeregning(benyttetSivilstand = BorMedTypeEnum.J_EKTEF)
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
    }

    test("createBeregning should handle different benyttetSivilstand values") {
        val beregning = createBeregning(benyttetSivilstand = BorMedTypeEnum.SAMBOER1_5)
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.SAMBOER1_5
    }

    // --- Tests for kapittel 20 data ---

    test("createBeregning should set beregningsMetodeEnum from beregningKapittel20") {
        val beregning = createBeregning(beregningsMetode = BeregningsmetodeEnum.FOLKETRYGD)
        beregning.beregningsMetodeEnum shouldBe BeregningsmetodeEnum.FOLKETRYGD
    }

    test("createBeregning should set EOS beregningsMetodeEnum") {
        val beregning = createBeregning(beregningsMetode = BeregningsmetodeEnum.EOS)
        beregning.beregningsMetodeEnum shouldBe BeregningsmetodeEnum.EOS
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

    test("createBeregning should set resultatTypeEnum from beregningKapittel20") {
        val beregning = createBeregning(resultatType = ResultattypeEnum.AP2025)
        beregning.resultatTypeEnum shouldBe ResultattypeEnum.AP2025
    }

    test("createBeregning should set beholdninger from beregningKapittel20") {
        val beholdninger = Beholdninger()
        val beregning = createBeregning(beholdninger = beholdninger)
        beregning.beholdninger shouldBe beholdninger
    }

    // --- Tests for beregningsInformasjon ---

    test("createBeregning should set epsMottarPensjon from beregningsInformasjonKapittel20") {
        val beregning = createBeregning(epsMottarPensjon = true)
        beregning.epsMottarPensjon shouldBe true
    }

    test("createBeregning should set epsMottarPensjon false") {
        val beregning = createBeregning(epsMottarPensjon = false)
        beregning.epsMottarPensjon shouldBe false
    }

    test("createBeregning should set gjenlevenderettAnvendt from beregningsInformasjonKapittel20") {
        val beregning = createBeregning(gjenlevenderettAnvendt = true)
        beregning.gjenlevenderettAnvendt shouldBe true
    }

    test("createBeregning should set gjenlevenderettAnvendt false") {
        val beregning = createBeregning(gjenlevenderettAnvendt = false)
        beregning.gjenlevenderettAnvendt shouldBe false
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

    test("createBeregning should not set alternativ konvensjon data when no tapende delberegning") {
        val beregning = createBeregning(
            beregningsMetode = BeregningsmetodeEnum.FOLKETRYGD,
            tapendeDelberegning = null
        )

        beregning.pensjonUnderUtbetaling2025AltKonv shouldBe null
        beregning.beholdningerAltKonv shouldBe null
    }

    test("createBeregning should filter alternativ konvensjon ytelseskomponenter") {
        val beregning = createBeregning(
            beregningsMetode = BeregningsmetodeEnum.FOLKETRYGD,
            tapendeDelberegning = AldersberegningKapittel20().apply {
                beregningsMetodeEnum = BeregningsmetodeEnum.EOS
                pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                    ytelseskomponenter = mutableListOf(
                        Garantipensjon().apply { brukt = true; opphort = false; brutto = 100 },
                        Inntektspensjon().apply { brukt = false; opphort = false; brutto = 200 } // irrelevant
                    )
                }
            }
        )

        beregning.pensjonUnderUtbetaling2025AltKonv?.ytelseskomponenter?.size shouldBe 1
        beregning.pensjonUnderUtbetaling2025AltKonv?.ytelseskomponenter?.get(0)?.brutto shouldBe 100
    }

    // --- Tests from base class populate() ---

    test("createBeregning should set sivilstandTypeEnum from forrigeKravhode søker detalj") {
        val beregning = createBeregning(
            forrigeKravhode = kravhodeWithSoker(SivilstandEnum.GIFT)
        )
        beregning.sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
    }

    test("createBeregning should set sivilstandTypeEnum UGIF") {
        val beregning = createBeregning(
            forrigeKravhode = kravhodeWithSoker(SivilstandEnum.UGIF)
        )
        beregning.sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    test("createBeregning should set vilkarsvedtakEktefelletillegg from filtrertVilkarsvedtakList") {
        val etVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.ET }
        val beregning = createBeregning(
            filtrertVilkarsvedtakList = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                etVedtak
            )
        )
        beregning.vilkarsvedtakEktefelletillegg shouldBe etVedtak
    }

    test("createBeregning should not set vilkarsvedtakEktefelletillegg when no ET vedtak") {
        val beregning = createBeregning(
            filtrertVilkarsvedtakList = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            )
        )
        beregning.vilkarsvedtakEktefelletillegg shouldBe null
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

    test("createBeregning should not set avdodesPersongrunnlag when no AVDOD role") {
        val beregning = createBeregning(
            forrigeKravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
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
        )
        beregning.avdodesPersongrunnlag shouldBe null
    }

    test("createBeregning should set eps from forrigeKravhode with EKTEF role") {
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

    test("createBeregning should set eps from forrigeKravhode with SAMBO role") {
        val epsGrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = 88L }
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SAMBO
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

    // --- Integration-style tests ---

    test("createBeregning should correctly populate all fields from full spec") {
        val virkFom = dateAtNoon(2025, Calendar.JUNE, 1)
        val beholdninger = Beholdninger()
        val brok = Brok().apply { teller = 35; nevner = 40 }

        val beregning = createBeregning(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            virkFom = virkFom,
            benyttetSivilstand = BorMedTypeEnum.J_EKTEF,
            beregningsMetode = BeregningsmetodeEnum.FOLKETRYGD,
            prorataBrok = brok,
            tt_anv_kapittel20 = 40,
            resultatType = ResultattypeEnum.AP2025,
            beholdninger = beholdninger,
            epsMottarPensjon = true,
            gjenlevenderettAnvendt = false,
            ytelseskomponentListe = mutableListOf(
                Inntektspensjon().apply { brukt = true; opphort = false; brutto = 5000 }
            ),
            forrigeKravhode = kravhodeWithSoker(SivilstandEnum.GIFT)
        )

        beregning.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
        beregning.virkDato shouldBe virkFom
        beregning.benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
        beregning.beregningsMetodeEnum shouldBe BeregningsmetodeEnum.FOLKETRYGD
        beregning.prorataBrok_kap_20?.teller shouldBe 35
        beregning.tt_anv_kap_20 shouldBe 40
        beregning.resultatTypeEnum shouldBe ResultattypeEnum.AP2025
        beregning.beholdninger shouldBe beholdninger
        beregning.epsMottarPensjon shouldBe true
        beregning.gjenlevenderettAnvendt shouldBe false
        beregning.pensjonUnderUtbetaling?.ytelseskomponenter?.size shouldBe 1
        beregning.sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
    }
})

private fun createBeregning(
    ytelseskomponentListe: MutableList<Ytelseskomponent> = mutableListOf(),
    regelverkType: RegelverkTypeEnum? = RegelverkTypeEnum.N_REG_N_OPPTJ,
    virkFom: Date? = null,
    benyttetSivilstand: BorMedTypeEnum? = null,
    beregningsMetode: BeregningsmetodeEnum? = null,
    prorataBrok: Brok? = null,
    tt_anv_kapittel20: Int? = null,
    resultatType: ResultattypeEnum? = null,
    beholdninger: Beholdninger? = null,
    epsMottarPensjon: Boolean? = null,
    gjenlevenderettAnvendt: Boolean? = null,
    tapendeDelberegning: AldersberegningKapittel20? = null,
    filtrertVilkarsvedtakList: List<VilkarsVedtak> = emptyList(),
    forrigeKravhode: Kravhode? = null
): SisteAldersberegning2011 {
    val spec = SisteBeregningSpec(
        beregningsresultat = BeregningsResultatAlderspensjon2025().apply {
            this.virkFom = virkFom
            this.benyttetSivilstandEnum = benyttetSivilstand

            if (ytelseskomponentListe.isNotEmpty()) {
                pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                    ytelseskomponenter = ytelseskomponentListe
                }
            }

            beregningKapittel20 = AldersberegningKapittel20().apply {
                beregningsMetodeEnum = beregningsMetode
                this.prorataBrok = prorataBrok
                tt_anv = tt_anv_kapittel20 ?: 0
                this.beholdninger = beholdninger
                resultatTypeEnum = resultatType

                if (tapendeDelberegning != null) {
                    delberegning2011Liste = mutableListOf(
                        BeregningRelasjon().apply {
                            beregning2011 = tapendeDelberegning
                        }
                    )
                }
            }

            beregningsInformasjonKapittel20 = BeregningsInformasjon().apply {
                this.epsMottarPensjon = epsMottarPensjon ?: false
                this.gjenlevenderettAnvendt = gjenlevenderettAnvendt ?: false
            }
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

    return Alderspensjon2025SisteBeregningCreator(kravService = mockk()).createBeregning(
        spec,
        BeregningsResultatAlderspensjon2025()
    ) as SisteAldersberegning2011
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
