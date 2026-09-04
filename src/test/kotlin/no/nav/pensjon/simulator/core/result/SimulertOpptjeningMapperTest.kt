package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AldersberegningKapittel20
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.DagpengetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UforetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import java.time.LocalDate

class SimulertOpptjeningMapperTest : ShouldSpec({

    should("hente 'pensjonsgivende inntekt'-pensjonspoeng fra poengtallets 'pp'-verdi") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = poengtallListe(aar = 2024, pensjonspoeng = 1.23),
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 1.23
    }

    context("ingen poengtall for angitt år, ikke bruke 'null' som default pensjonspoeng") {
        should("gi 0 i pensjonpoeng") {
            SimulertOpptjeningMapper.simulertOpptjening(
                aar = 2024,
                resultatListe = emptyList(),
                soekerGrunnlag = Persongrunnlag(),
                poengtallListe = poengtallListe(aar = 2023, pensjonspoeng = 2.1), // annet år
                useNullAsDefaultPensjonspoeng = false
            ).pensjonsgivendeInntektPensjonspoeng shouldBe 0.0
        }
    }

    context("ingen poengtall, bruke 'null' som default pensjonspoeng") {
        should("gi udefinert pensjonpoeng-verdi") {
            SimulertOpptjeningMapper.simulertOpptjening(
                aar = 2023,
                resultatListe = emptyList(),
                soekerGrunnlag = Persongrunnlag(),
                poengtallListe = emptyList(),
                useNullAsDefaultPensjonspoeng = true
            ).pensjonsgivendeInntektPensjonspoeng shouldBe null
        }
    }

    should("hente pensjonsgivende inntekt fra opptjeningsgrunnlaget") {
        val persongrunnlag = Persongrunnlag().apply {
            opptjeningsgrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2024
                    pi = 750000
                    opptjeningTypeEnum = OpptjeningtypeEnum.PPI
                }
            )
        }

        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).pensjonsgivendeInntekt shouldBe 750000
    }

    should("bruke omsorgspoengene med høyest prioritet") {
        val persongrunnlag = Persongrunnlag().apply {
            opptjeningsgrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2024
                    pp = 3.5
                    opptjeningTypeEnum = OpptjeningtypeEnum.OBU7 // lower priority
                },
                Opptjeningsgrunnlag().apply {
                    ar = 2024
                    pp = 3.0
                    opptjeningTypeEnum = OpptjeningtypeEnum.OSFE // highest priority
                }
            )
        }

        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).omsorgPensjonspoeng shouldBe 3.0 // OSFE has highest priority
    }

    should("hente pensjonsbeholdning fra persongrunnlaget") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = beholdningsgrunnlag(beloep = 3500000),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).pensjonBeholdning shouldBe 3500000
    }

    should("hente omsorg fra omsorgsgrunnlagslisten") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = omsorgsgrunnlag(aar = 2024),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).omsorg shouldBe true
    }

    should("return false for omsorg when no omsorgsgrunnlag for year") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = omsorgsgrunnlag(aar = 2023), // annet år
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).omsorg shouldBe false
    }

    should("gi 'true' for dagpenger når det finnes et ordinært dagpengegrunnlag") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = dagpengegrunnlag(aar = 2024, type = DagpengetypeEnum.DP),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).dagpenger shouldBe true
    }

    should("gi 'true' for dagpenger for fiskere når det finnes et dagpengegrunnlag for fiskere") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2023,
            resultatListe = emptyList(),
            soekerGrunnlag = dagpengegrunnlag(aar = 2023, type = DagpengetypeEnum.DP_FF),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).dagpengerFiskere shouldBe true
    }

    should("gi 'true' for førstegangstjeneste når det finnes en førstegangstjenesteperiode") {
        val persongrunnlag = Persongrunnlag().apply {
            forstegangstjenestegrunnlag = Forstegangstjeneste().apply {
                periodeListe = mutableListOf(
                    ForstegangstjenestePeriode().apply { fomDatoLd = fom }
                )
            }
        }

        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).foerstegangstjeneste shouldBe true
    }

    should("gi 'true' for 'har uføre' når det finnes en uføreperiode") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = ufoeregrunnlag(type = UforetypeEnum.UF_M_YRKE),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).harUfoere shouldBe true
    }

    should("ignorere uføreperioder av type 'virk ikke ufør'") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = ufoeregrunnlag(type = UforetypeEnum.VIRK_IKKE_UFOR), // skal filtreres bort
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).harUfoere shouldBe false
    }

    should("gi 'true' for offentlig AFP når det finnes en periode med AFP i offentlig sektor") {
        val persongrunnlag = offentligAfpGrunnlag(
            afpHistorikkListe = listOf(AfpHistorikk().apply { virkFomLd = fom; virkTomLd = tom })
        )

        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).harOffentligAfp shouldBe true
    }

    should("gi 'false' for offentlig AFP når det ikke finnes noen periode med AFP i offentlig sektor") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = offentligAfpGrunnlag(afpHistorikkListe = emptyList()),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).harOffentligAfp shouldBe false
    }

    context("persongrunnlaget inneholder ikke pensjonsbeholdning") {
        context("ren kapittel 20") {
            should("hente pensjonsbeholdningen fra beregningsresultatet 'type 2025'") {
                SimulertOpptjeningMapper.simulertOpptjening(
                    aar = 2024,
                    resultatListe = listOf(alderspensjonsresultat2025(pensjonsbeholdning = 4200000)),
                    soekerGrunnlag = Persongrunnlag(),
                    poengtallListe = emptyList(),
                    useNullAsDefaultPensjonspoeng = true
                ).pensjonBeholdning shouldBe 4200000
            }
        }

        context("overgangskull") {
            should("hente pensjonsbeholdningen fra beregningsresultatet 'type 2016'") {
                val beregningsResultat = BeregningsResultatAlderspensjon2016().apply {
                    virkFomLd = fom
                    beregningsResultat2025 = alderspensjonsresultat2025(pensjonsbeholdning = 4300000)
                }

                SimulertOpptjeningMapper.simulertOpptjening(
                    aar = 2024,
                    resultatListe = listOf(beregningsResultat),
                    soekerGrunnlag = Persongrunnlag(),
                    poengtallListe = emptyList(),
                    useNullAsDefaultPensjonspoeng = true
                ).pensjonBeholdning shouldBe 4300000
            }
        }
    }

    context("både persongrunnlaget og beregningsresultatet inneholder pensjonsbeholdning") {
        should("bruke verdien fra beregningsresultatet") {
            SimulertOpptjeningMapper.simulertOpptjening(
                aar = 2024,
                resultatListe = listOf(alderspensjonsresultat2025(pensjonsbeholdning = 4500000)),
                soekerGrunnlag = beholdningsgrunnlag(beloep = 5300000),
                poengtallListe = emptyList(),
                useNullAsDefaultPensjonspoeng = true
            ).pensjonBeholdning shouldBe 4500000
        }
    }

    context("beregnet pensjonsbeholdning gjelder siste del av året") {
        should("bruke beregnet pensjonsbeholdning") {
            SimulertOpptjeningMapper.simulertOpptjening(
                aar = 2024,
                resultatListe = listOf(
                    alderspensjonsresultat2025(
                        pensjonsbeholdning = 4500000,
                        virkningFom = LocalDate.of(2024, 12, 1)
                    )
                ),
                soekerGrunnlag = beholdningsgrunnlag(beloep = 5300000),
                poengtallListe = emptyList(),
                useNullAsDefaultPensjonspoeng = true
            ).pensjonBeholdning shouldBe 4500000
        }
    }
})

private val fom: LocalDate = LocalDate.of(2024, 1, 1)
private val tom: LocalDate = LocalDate.of(2024, 12, 31)

private fun beholdningsgrunnlag(beloep: Int): Persongrunnlag = Persongrunnlag().apply {
    beholdninger = mutableListOf(
        Pensjonsbeholdning().apply {
            ar = 2024
            totalbelop = beloep.toDouble()
            beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
        }
    )
}

private fun offentligAfpGrunnlag(afpHistorikkListe: List<AfpHistorikk>) =
    Persongrunnlag().apply { this.afpHistorikkListe = afpHistorikkListe }

private fun dagpengegrunnlag(aar: Int, type: DagpengetypeEnum) =
    Persongrunnlag().apply {
        dagpengegrunnlagListe = mutableListOf(
            Dagpengegrunnlag().apply { ar = aar; dagpengetypeEnum = type }
        )
    }

private fun omsorgsgrunnlag(aar: Int) =
    Persongrunnlag().apply {
        omsorgsgrunnlagListe = mutableListOf(Omsorgsgrunnlag().apply { ar = aar })
    }

private fun poengtallListe(aar: Int, pensjonspoeng: Double): List<Poengtall> =
    listOf(Poengtall().apply { ar = aar; pp = pensjonspoeng })

private fun ufoeregrunnlag(type: UforetypeEnum) =
    Persongrunnlag().apply {
        uforeHistorikk = Uforehistorikk().apply {
            uforeperiodeListe = mutableListOf(
                Uforeperiode().apply {
                    ufgFomLd = fom
                    ufgTomLd = tom
                    uforeTypeEnum = type
                }
            )
        }
    }

private fun alderspensjonsresultat2025(pensjonsbeholdning: Int, virkningFom: LocalDate = fom) =
    BeregningsResultatAlderspensjon2025().apply {
        virkFomLd = virkningFom
        uttaksgrad = 100
        beregningKapittel20 = AldersberegningKapittel20().apply {
            beholdninger = Beholdninger().apply {
                beholdninger = listOf(
                    Pensjonsbeholdning().apply {
                        ar = 2024
                        totalbelop = pensjonsbeholdning.toDouble()
                        beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                    }
                )
            }
        }
    }