package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
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
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

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
            ytelseskomponenter shouldHaveSize 2
            ytelseskomponenter[0].brutto shouldBe 100
            ytelseskomponenter[1].brutto shouldBe 400
        }
    }

    test("createBeregning should handle empty ytelseskomponenter list") {
        createBeregning(
            ytelseskomponentListe = mutableListOf(
                Garantipensjon().apply { brukt = true; opphort = true; brutto = 100 } // all irrelevant
            )
        ).pensjonUnderUtbetaling?.ytelseskomponenter!! shouldHaveSize 0
    }

    // --- Tests for regelverkTypeEnum ---

    test("createBeregning should set regelverkTypeEnum from spec") {
        createBeregning(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ
        ).regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
    }

    test("createBeregning should handle null regelverkTypeEnum") {
        createBeregning(regelverkType = null).regelverkTypeEnum shouldBe null
    }

    // --- Tests for virkDato ---

    test("createBeregning should set virkDato from beregningsresultat") {
        val virkFom = LocalDate.of(2025, 3, 1)
        createBeregning(virkFom = virkFom).virkDato shouldBe virkFom.toNorwegianDateAtNoon()
    }

    test("createBeregning should handle null virkFom") {
        createBeregning(virkFom = null).virkDato shouldBe null
    }

    // --- Tests for benyttetSivilstand ---

    test("createBeregning should set benyttetSivilstandEnum from beregningsresultat") {
        createBeregning(
            benyttetSivilstand = BorMedTypeEnum.J_EKTEF
        ).benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
    }

    test("createBeregning should handle different benyttetSivilstand values") {
        createBeregning(
            benyttetSivilstand = BorMedTypeEnum.SAMBOER1_5
        ).benyttetSivilstandEnum shouldBe BorMedTypeEnum.SAMBOER1_5
    }

    // --- Tests for kapittel 20 data ---

    test("createBeregning should set beregningsMetodeEnum from beregningKapittel20") {
        createBeregning(
            beregningsmetode = BeregningsmetodeEnum.FOLKETRYGD
        ).beregningsMetodeEnum shouldBe BeregningsmetodeEnum.FOLKETRYGD
    }

    test("createBeregning should set EOS beregningsMetodeEnum") {
        createBeregning(
            beregningsmetode = BeregningsmetodeEnum.EOS
        ).beregningsMetodeEnum shouldBe BeregningsmetodeEnum.EOS
    }

    test("createBeregning should set prorataBrok_kap_20 from beregningKapittel20") {
        val beregning = createBeregning(prorataBroek = Brok().apply { teller = 30; nevner = 40 })

        with(beregning.prorataBrok_kap_20!!) {
            teller shouldBe 30
            nevner shouldBe 40
        }
    }

    test("createBeregning should set tt_anv_kap_20 from beregningKapittel20") {
        createBeregning(anvendtTrygdetid = 35).tt_anv_kap_20 shouldBe 35
    }

    test("createBeregning should set resultatTypeEnum from beregningKapittel20") {
        createBeregning(resultatType = ResultattypeEnum.AP2025).resultatTypeEnum shouldBe ResultattypeEnum.AP2025
    }

    test("createBeregning should set beholdninger from beregningKapittel20") {
        val beholdninger = Beholdninger()
        createBeregning(beholdninger = beholdninger).beholdninger shouldBe beholdninger
    }

    // --- Tests for beregningsInformasjon ---

    test("createBeregning should set epsMottarPensjon from beregningsInformasjonKapittel20") {
        createBeregning(epsMottarPensjon = true).epsMottarPensjon shouldBe true
    }

    test("createBeregning should set epsMottarPensjon false") {
        createBeregning(epsMottarPensjon = false).epsMottarPensjon shouldBe false
    }

    test("createBeregning should set gjenlevenderettAnvendt from beregningsInformasjonKapittel20") {
        createBeregning(gjenlevenderettAnvendt = true).gjenlevenderettAnvendt shouldBe true
    }

    test("createBeregning should set gjenlevenderettAnvendt false") {
        createBeregning(gjenlevenderettAnvendt = false).gjenlevenderettAnvendt shouldBe false
    }

    // --- Tests for alternativ konvensjon data (tapende delberegning) ---

    test("createBeregning should set alternativ konvensjon data from tapende delberegning") {
        val tapendeYtelseskomponenter = mutableListOf<Ytelseskomponent>(
            Garantipensjon().apply { brukt = true; opphort = false; brutto = 999 }
        )
        val tapendeBeholdninger = Beholdninger()
        val tapendeBroek = Brok().apply { teller = 10; nevner = 20 }

        val beregning = createBeregning(
            beregningsmetode = BeregningsmetodeEnum.FOLKETRYGD,
            tapendeDelberegning = AldersberegningKapittel20().apply {
                beregningsMetodeEnum = BeregningsmetodeEnum.EOS // different from winning
                pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                    ytelseskomponenter = tapendeYtelseskomponenter
                }
                beholdninger = tapendeBeholdninger
                prorataBrok = tapendeBroek
                tt_anv = 25
            }
        )

        with(beregning) {
            pensjonUnderUtbetaling2025AltKonv shouldNotBe null
            with(pensjonUnderUtbetaling2025AltKonv!!) {
                ytelseskomponenter shouldHaveSize 1
                ytelseskomponenter[0].brutto shouldBe 999
            }
            beholdningerAltKonv shouldBe tapendeBeholdninger
            with(prorataBrok_kap_20AltKonv!!) {
                teller shouldBe 10
                nevner shouldBe 20
            }
            tt_anv_kap_20AltKonv shouldBe 25
        }
    }

    test("createBeregning should not set alternativ konvensjon data when no tapende delberegning") {
        val beregning = createBeregning(
            beregningsmetode = BeregningsmetodeEnum.FOLKETRYGD,
            tapendeDelberegning = null
        )

        with(beregning) {
            pensjonUnderUtbetaling2025AltKonv shouldBe null
            beholdningerAltKonv shouldBe null
        }
    }

    test("createBeregning should filter alternativ konvensjon ytelseskomponenter") {
        val beregning = createBeregning(
            beregningsmetode = BeregningsmetodeEnum.FOLKETRYGD,
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

        with(beregning.pensjonUnderUtbetaling2025AltKonv!!) {
            ytelseskomponenter shouldHaveSize 1
            ytelseskomponenter[0].brutto shouldBe 100
        }
    }

    // --- Tests from base class populate() ---

    test("createBeregning should set sivilstandTypeEnum from forrigeKravhode søker detalj") {
        createBeregning(
            forrigeKravhode = kravhodeWithSoeker(SivilstandEnum.GIFT)
        ).sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
    }

    test("createBeregning should set sivilstandTypeEnum UGIF") {
        createBeregning(
            forrigeKravhode = kravhodeWithSoeker(SivilstandEnum.UGIF)
        ).sivilstandTypeEnum shouldBe SivilstandEnum.UGIF
    }

    test("createBeregning should set vilkarsvedtakEktefelletillegg from filtrertVilkarsvedtakList") {
        val ektefelletilleggVedtak = VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.ET }

        createBeregning(
            filtrertVilkaarsvedtakListe = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                ektefelletilleggVedtak
            )
        ).vilkarsvedtakEktefelletillegg shouldBe ektefelletilleggVedtak
    }

    test("createBeregning should not set vilkarsvedtakEktefelletillegg when no ET vedtak") {
        createBeregning(
            filtrertVilkaarsvedtakListe = listOf(
                VilkarsVedtak().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP }
            )
        ).vilkarsvedtakEktefelletillegg shouldBe null
    }

    test("createBeregning should set avdodesPersongrunnlag from forrigeKravhode") {
        val avdoedGrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = 99L }
            personDetaljListe = mutableListOf(persondetalj(grunnlagsrolle = GrunnlagsrolleEnum.AVDOD))
        }

        createBeregning(
            forrigeKravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(avdoedGrunnlag) }
        ).avdodesPersongrunnlag shouldBe avdoedGrunnlag
    }

    test("createBeregning should not set avdodesPersongrunnlag when no AVDOD role") {
        createBeregning(
            forrigeKravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        penPerson = PenPerson().apply { penPersonId = 1L }
                        personDetaljListe = mutableListOf(persondetalj(grunnlagsrolle = GrunnlagsrolleEnum.SOKER))
                    }
                )
            }
        ).avdodesPersongrunnlag shouldBe null
    }

    test("createBeregning should set eps from forrigeKravhode with EKTEF role") {
        val epsGrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = 88L }
            personDetaljListe = mutableListOf(persondetalj(grunnlagsrolle = GrunnlagsrolleEnum.EKTEF))
        }

        createBeregning(
            forrigeKravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(epsGrunnlag) }
        ).eps shouldNotBe null
    }

    test("createBeregning should set eps from forrigeKravhode with SAMBO role") {
        val epsGrunnlag = Persongrunnlag().apply {
            penPerson = PenPerson().apply { penPersonId = 88L }
            personDetaljListe = mutableListOf(persondetalj(grunnlagsrolle = GrunnlagsrolleEnum.SAMBO))
        }

        createBeregning(
            forrigeKravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(epsGrunnlag) }
        ).eps shouldNotBe null
    }

    // --- Integration-style tests ---

    test("createBeregning should correctly populate all fields from full spec") {
        val virkFom = LocalDate.of(2025, 6, 1)
        val beholdninger = Beholdninger()

        val beregning = createBeregning(
            regelverkType = RegelverkTypeEnum.N_REG_N_OPPTJ,
            virkFom = virkFom,
            benyttetSivilstand = BorMedTypeEnum.J_EKTEF,
            beregningsmetode = BeregningsmetodeEnum.FOLKETRYGD,
            prorataBroek = Brok().apply { teller = 35; nevner = 40 },
            anvendtTrygdetid = 40,
            resultatType = ResultattypeEnum.AP2025,
            beholdninger = beholdninger,
            epsMottarPensjon = true,
            gjenlevenderettAnvendt = false,
            ytelseskomponentListe = mutableListOf(
                Inntektspensjon().apply { brukt = true; opphort = false; brutto = 5000 }
            ),
            forrigeKravhode = kravhodeWithSoeker(SivilstandEnum.GIFT)
        )

        with(beregning) {
            regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
            virkDato shouldBe virkFom.toNorwegianDateAtNoon()
            benyttetSivilstandEnum shouldBe BorMedTypeEnum.J_EKTEF
            beregningsMetodeEnum shouldBe BeregningsmetodeEnum.FOLKETRYGD
            prorataBrok_kap_20?.teller shouldBe 35
            tt_anv_kap_20 shouldBe 40
            resultatTypeEnum shouldBe ResultattypeEnum.AP2025
            this.beholdninger shouldBe beholdninger
            epsMottarPensjon shouldBe true
            gjenlevenderettAnvendt shouldBe false
            pensjonUnderUtbetaling?.ytelseskomponenter?.size shouldBe 1
            sivilstandTypeEnum shouldBe SivilstandEnum.GIFT
        }
    }
})

private fun persondetalj(grunnlagsrolle: GrunnlagsrolleEnum) =
    PersonDetalj().apply {
        bruk = true
        grunnlagsrolleEnum = grunnlagsrolle
    }

private fun createBeregning(
    ytelseskomponentListe: MutableList<Ytelseskomponent> = mutableListOf(),
    regelverkType: RegelverkTypeEnum? = RegelverkTypeEnum.N_REG_N_OPPTJ,
    virkFom: LocalDate? = null,
    benyttetSivilstand: BorMedTypeEnum? = null,
    beregningsmetode: BeregningsmetodeEnum? = null,
    prorataBroek: Brok? = null,
    anvendtTrygdetid: Int? = null,
    resultatType: ResultattypeEnum? = null,
    beholdninger: Beholdninger? = null,
    epsMottarPensjon: Boolean? = null,
    gjenlevenderettAnvendt: Boolean? = null,
    tapendeDelberegning: AldersberegningKapittel20? = null,
    filtrertVilkaarsvedtakListe: List<VilkarsVedtak> = emptyList(),
    forrigeKravhode: Kravhode? = null
): SisteAldersberegning2011 {
    val spec = SisteBeregningSpec(
        beregningsresultat = BeregningsResultatAlderspensjon2025().apply {
            this.virkFomLd = virkFom
            this.benyttetSivilstandEnum = benyttetSivilstand

            if (ytelseskomponentListe.isNotEmpty()) {
                pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply {
                    ytelseskomponenter = ytelseskomponentListe
                }
            }

            beregningKapittel20 = AldersberegningKapittel20().apply {
                beregningsMetodeEnum = beregningsmetode
                prorataBrok = prorataBroek
                tt_anv = anvendtTrygdetid ?: 0
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
        filtrertVilkarsvedtakList = filtrertVilkaarsvedtakListe,
        isRegelverk1967 = false,
        vilkarsvedtakListe = emptyList(),
        kravhode = null,
        beregning = null,
        fomDato = null,
        tomDato = null,
        regelverk1967VirkToEarly = false
    )

    return Alderspensjon2025SisteBeregningCreator(kravService = mockk())
        .createBeregning(spec, beregningResultat = BeregningsResultatAlderspensjon2025())
}

private fun kravhodeWithSoeker(sivilstand: SivilstandEnum) =
    Kravhode().apply {
        persongrunnlagListe = mutableListOf(
            Persongrunnlag().apply {
                penPerson = PenPerson().apply { penPersonId = 1L }
                personDetaljListe = mutableListOf(
                    persondetalj(grunnlagsrolle = GrunnlagsrolleEnum.SOKER).apply {
                        sivilstandTypeEnum = sivilstand
                    }
                )
            }
        )
    }
