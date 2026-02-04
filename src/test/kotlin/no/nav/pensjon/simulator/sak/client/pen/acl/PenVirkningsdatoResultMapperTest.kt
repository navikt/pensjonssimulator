package no.nav.pensjon.simulator.sak.client.pen.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.FppGarantiKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.Fravik_19_3_Enum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtilleggEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ProRataBeregningTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UforetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import java.time.LocalDate

class PenVirkningsdatoResultMapperTest : FunSpec({

    context("fromDto") {

        test("mapper tom liste") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = emptyList()
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe.shouldBeEmpty()
        }

        test("mapper virkningsdato og kravFremsattDato") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        virkningsdato = LocalDate.of(2025, 1, 1),
                        kravFremsattDato = LocalDate.of(2024, 6, 15)
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe shouldHaveSize 1
            result.foersteVirkningDatoGrunnlagListe[0].virkningsdato shouldNotBe null
            result.foersteVirkningDatoGrunnlagListe[0].kravFremsattDato shouldNotBe null
        }

        test("mapper kravlinjeType til enum") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        kravlinjeType = "AP"
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
        }

        test("mapper null kravlinjeType til null") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        kravlinjeType = null
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].kravlinjeTypeEnum shouldBe null
        }

        test("mapper bruker med penPersonId og pid") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            pid = "12345678901",
                            fodselsdato = LocalDate.of(1963, 5, 15)
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker shouldNotBe null
            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.penPersonId shouldBe 123L
            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.pid?.value shouldBe "12345678901"
            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.foedselsdato shouldBe LocalDate.of(1963, 5, 15)
        }

        test("mapper bruker med fnr prioritert over pid") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            fnr = "11111111111",
                            pid = "22222222222"
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            // fnr skal prioriteres over pid
            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.pid?.value shouldBe "11111111111"
        }

        test("mapper annenPerson") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        annenPerson = PenSpecialPenPerson(
                            penPersonId = 456L,
                            pid = "98765432109"
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].annenPerson shouldNotBe null
            result.foersteVirkningDatoGrunnlagListe[0].annenPerson!!.penPersonId shouldBe 456L
        }

        test("mapper flere virkningsdatoGrunnlag") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(kravlinjeType = "AP"),
                    PenSpecialForsteVirkningsdatoGrunnlag(kravlinjeType = "UP"),
                    PenSpecialForsteVirkningsdatoGrunnlag(kravlinjeType = "GJP")
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe shouldHaveSize 3
            result.foersteVirkningDatoGrunnlagListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
            result.foersteVirkningDatoGrunnlagListe[1].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.UP
            result.foersteVirkningDatoGrunnlagListe[2].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.GJP
        }
    }

    context("afpHistorikk mapping") {

        test("mapper afpHistorikkListe") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            afpHistorikkListe = mutableListOf(
                                PenAfpHistorikk(
                                    afpFpp = 3.5,
                                    virkFom = LocalDate.of(2020, 1, 1),
                                    virkTom = LocalDate.of(2024, 12, 31),
                                    afpPensjonsgrad = 50,
                                    afpOrdning = "LONHO"
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val afpHistorikkListe = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.afpHistorikkListe!!
            afpHistorikkListe shouldHaveSize 1
            afpHistorikkListe[0].afpFpp shouldBe 3.5
            afpHistorikkListe[0].afpPensjonsgrad shouldBe 50
            afpHistorikkListe[0].afpOrdningEnum shouldBe AFPtypeEnum.LONHO
        }

        test("mapper tom afpHistorikkListe") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            afpHistorikkListe = mutableListOf()
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.afpHistorikkListe!!.shouldBeEmpty()
        }

        test("mapper null afpHistorikkListe til tom liste") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            afpHistorikkListe = null
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.afpHistorikkListe!!.shouldBeEmpty()
        }
    }

    context("uforehistorikk mapping") {

        test("mapper uforehistorikk med uforeperioder") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            uforehistorikk = PenUfoerehistorikk(
                                uforeperiodeListe = listOf(
                                    PenUfoereperiode(
                                        ufg = 50,
                                        uft = LocalDate.of(2020, 1, 1),
                                        uforeType = "UFORE",
                                        fppGaranti = 3.3,
                                        fppGarantiKode = "UNG_UF_MR_33_PO",
                                        ufgFom = LocalDate.of(2020, 1, 1),
                                        ufgTom = LocalDate.of(2025, 12, 31)
                                    )
                                ),
                                garantigrad = 50,
                                garantigradYrke = 0,
                                sistMedlITrygden = LocalDate.of(2019, 12, 31)
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val uforehistorikk = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.uforehistorikk!!
            uforehistorikk.garantigrad shouldBe 50
            uforehistorikk.garantigradYrke shouldBe 0
            uforehistorikk.sistMedlITrygden shouldNotBe null
            uforehistorikk.uforeperiodeListe shouldHaveSize 1

            val uforeperiode = uforehistorikk.uforeperiodeListe[0]
            uforeperiode.ufg shouldBe 50
            uforeperiode.uforeTypeEnum shouldBe UforetypeEnum.UFORE
            uforeperiode.fppGaranti shouldBe 3.3
            uforeperiode.fppGarantiKodeEnum shouldBe FppGarantiKodeEnum.UNG_UF_MR_33_PO
        }

        test("mapper uforeperiode med proRataBeregningType") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            uforehistorikk = PenUfoerehistorikk(
                                uforeperiodeListe = listOf(
                                    PenUfoereperiode(
                                        proRataBeregningType = "KUN_EOS"
                                    )
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val uforeperiode = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.uforehistorikk!!.uforeperiodeListe[0]
            uforeperiode.proRataBeregningTypeEnum shouldBe ProRataBeregningTypeEnum.KUN_EOS
        }

        test("mapper uforeperiode med alle numeriske verdier") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            uforehistorikk = PenUfoerehistorikk(
                                uforeperiodeListe = listOf(
                                    PenUfoereperiode(
                                        spt = 4.5,
                                        spt_proRata = 3.2,
                                        opt = 2.1,
                                        ypt = 1.5,
                                        spt_pa_f92 = 10,
                                        spt_pa_e91 = 15,
                                        proRata_teller = 20,
                                        proRata_nevner = 40,
                                        beregningsgrunnlag = 500000,
                                        antattInntektFaktorKap19 = 0.8,
                                        antattInntektFaktorKap20 = 0.9
                                    )
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val uforeperiode = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.uforehistorikk!!.uforeperiodeListe[0]
            uforeperiode.spt shouldBe 4.5
            uforeperiode.spt_proRata shouldBe 3.2
            uforeperiode.opt shouldBe 2.1
            uforeperiode.ypt shouldBe 1.5
            uforeperiode.spt_pa_f92 shouldBe 10
            uforeperiode.spt_pa_e91 shouldBe 15
            uforeperiode.proRata_teller shouldBe 20
            uforeperiode.proRata_nevner shouldBe 40
            uforeperiode.beregningsgrunnlag shouldBe 500000
            uforeperiode.antattInntektFaktorKap19 shouldBe 0.8
            uforeperiode.antattInntektFaktorKap20 shouldBe 0.9
        }
    }

    context("generellHistorikk mapping") {

        test("mapper generellHistorikk med basisverdier") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                generellHistorikkId = 456L,
                                fravik_19_3 = "19_3_KODE_4",
                                fpp_eos = 2.5,
                                giftFor2011 = true
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val generellHistorikk = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!
            generellHistorikk.generellHistorikkId shouldBe 456L
            generellHistorikk.fravik_19_3Enum shouldBe Fravik_19_3_Enum.`19_3_KODE_4`
            generellHistorikk.fpp_eos shouldBe 2.5
            generellHistorikk.giftFor2011 shouldBe true
        }

        test("mapper generellHistorikk med poengtillegg") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                poengtillegg = "A"
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!.poengtilleggEnum shouldBe PoengtilleggEnum.A
        }

        test("mapper generellHistorikk med ventetilleggsgrunnlag") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                ventetilleggsgrunnlag = PenVentetilleggsgrunnlag(
                                    ventetilleggprosent = 12.5,
                                    vt_spt = 3.5,
                                    vt_opt = 2.0,
                                    vt_pa = 20,
                                    tt_vent = 40
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val ventetillegg = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!.ventetilleggsgrunnlag!!
            ventetillegg.ventetilleggprosent shouldBe 12.5
            ventetillegg.vt_spt shouldBe 3.5
            ventetillegg.vt_opt shouldBe 2.0
            ventetillegg.vt_pa shouldBe 20
            ventetillegg.tt_vent shouldBe 40
        }

        test("mapper generellHistorikk med eosEkstra") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                eosEkstra = PenEosEkstra(
                                    proRataBeregningType = "EOS_VANT",
                                    redusertAntFppAr = 5,
                                    spt_eos = 4.5,
                                    spt_pa_f92_eos = 10,
                                    spt_pa_e91_eos = 15,
                                    vilkar3_17Aok = true
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val eosEkstra = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!.eosEkstra!!
            eosEkstra.proRataBeregningTypeEnum shouldBe ProRataBeregningTypeEnum.EOS_VANT
            eosEkstra.redusertAntFppAr shouldBe 5
            eosEkstra.spt_eos shouldBe 4.5
            eosEkstra.spt_pa_f92_eos shouldBe 10
            eosEkstra.spt_pa_e91_eos shouldBe 15
            eosEkstra.vilkar3_17Aok shouldBe true
        }

        test("mapper generellHistorikk med garantiTrygdetid") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                garantiTrygdetid = PenGarantiTrygdetid(
                                    trygdetid_garanti = 40,
                                    fomDato = LocalDate.of(1980, 1, 1),
                                    tomDato = LocalDate.of(2020, 12, 31)
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val garantiTrygdetid = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!.garantiTrygdetid!!
            garantiTrygdetid.trygdetid_garanti shouldBe 40
            garantiTrygdetid.fomDato shouldNotBe null
            garantiTrygdetid.tomDato shouldNotBe null
        }

        test("mapper generellHistorikk med sertillegg1943kull") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                sertillegg1943kull = PenSaertillegg(
                                    pSats_st = 0.5,
                                    brutto = 10000,
                                    netto = 8000,
                                    fradrag = 2000,
                                    ytelsekomponentType = "ST",
                                    merknadListe = listOf(
                                        PenMerknad(kode = "MERKNAD1", argumentListe = listOf("arg1", "arg2"))
                                    ),
                                    sakType = "ALDER",
                                    formelKode = "GP1",
                                    brukt = true
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val saertillegg = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!.sertillegg1943kull!!
            saertillegg.pSats_st shouldBe 0.5
            saertillegg.brutto shouldBe 10000
            saertillegg.netto shouldBe 8000
            saertillegg.fradrag shouldBe 2000
            saertillegg.ytelsekomponentTypeEnum shouldBe YtelseskomponentTypeEnum.ST
            saertillegg.merknadListe shouldHaveSize 1
            saertillegg.merknadListe[0].kode shouldBe "MERKNAD1"
            saertillegg.merknadListe[0].argumentListe shouldBe listOf("arg1", "arg2")
            saertillegg.sakTypeEnum shouldBe SakTypeEnum.ALDER
            saertillegg.formelKodeEnum shouldBe FormelKodeEnum.GP1
            saertillegg.brukt shouldBe true
        }

        test("mapper saertillegg med reguleringsInformasjon") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = PenGenerellHistorikk(
                                sertillegg1943kull = PenSaertillegg(
                                    ytelsekomponentType = "ST",
                                    reguleringsInformasjon = PenReguleringsInformasjon(
                                        lonnsvekst = 0.03,
                                        fratrekksfaktor = 0.5,
                                        gammelG = 100000,
                                        nyG = 103000,
                                        reguleringsfaktor = 1.03,
                                        gjennomsnittligUttaksgradSisteAr = 100.0,
                                        reguleringsbelop = 3000.0,
                                        prisOgLonnsvekst = 0.025
                                    )
                                )
                            )
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            val regInfo = result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk!!.sertillegg1943kull!!.reguleringsInformasjon!!
            regInfo.lonnsvekst shouldBe 0.03
            regInfo.fratrekksfaktor shouldBe 0.5
            regInfo.gammelG shouldBe 100000
            regInfo.nyG shouldBe 103000
            regInfo.reguleringsfaktor shouldBe 1.03
            regInfo.gjennomsnittligUttaksgradSisteAr shouldBe 100.0
            regInfo.reguleringsbelop shouldBe 3000.0
            regInfo.prisOgLonnsvekst shouldBe 0.025
        }
    }

    context("null handling") {

        test("mapper null bruker til null") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = null
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker shouldBe null
        }

        test("mapper null uforehistorikk til null") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            uforehistorikk = null
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.uforehistorikk shouldBe null
        }

        test("mapper null generellHistorikk til null") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            generellHistorikk = null
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.generellHistorikk shouldBe null
        }

        test("mapper bruker uten fnr og pid til null pid") {
            val source = PenVirkningsdatoResult(
                forsteVirkningsdatoGrunnlagListe = listOf(
                    PenSpecialForsteVirkningsdatoGrunnlag(
                        bruker = PenSpecialPenPerson(
                            penPersonId = 123L,
                            fnr = null,
                            pid = null
                        )
                    )
                )
            )

            val result = PenVirkningsdatoResultMapper.fromDto(source)

            result.foersteVirkningDatoGrunnlagListe[0].bruker!!.pid shouldBe null
        }
    }
})
