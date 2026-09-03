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
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Dagpengegrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forstegangstjeneste
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForstegangstjenestePeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import java.time.LocalDate

class SimulertOpptjeningMapperTest : ShouldSpec({

    should("map poengtall.pp to pensjonsgivendeInntektPensjonspoeng") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = poengtallListe(aar = 2024, pensjonspoeng = 1.23),
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 1.23
    }

    should("return zero pensjonpoeng when no poengtall for angitt aar and useNullAsDefaultPensjonspoeng is false") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = poengtallListe(aar = 2023, pensjonspoeng = 2.1), // annet år
            useNullAsDefaultPensjonspoeng = false
        ).pensjonsgivendeInntektPensjonspoeng shouldBe 0.0
    }

    should("return undefined pensjonpoeng when no poengtall-liste is present and useNullAsDefaultPensjonspoeng is true") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2023,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).pensjonsgivendeInntektPensjonspoeng shouldBe null
    }

    should("map pensjonsgivendeInntekt from opptjeningsgrunnlag") {
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

    should("map omsorgPensjonspoeng with priority") {
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

    should("map pensjonBeholdning from soekerGrunnlag") {
        val persongrunnlag = Persongrunnlag().apply {
            beholdninger = mutableListOf(
                Pensjonsbeholdning().apply {
                    ar = 2024
                    totalbelop = 3500000.0
                    beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                }
            )
        }

        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = persongrunnlag,
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).pensjonBeholdning shouldBe 3500000
    }

    should("map omsorg from omsorgsgrunnlagListe") {
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

    should("map dagpenger") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = dagpengegrunnlag(aar = 2024, type = DagpengetypeEnum.DP),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).dagpenger shouldBe true
    }

    should("map dagpengerFiskere") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2023,
            resultatListe = emptyList(),
            soekerGrunnlag = dagpengegrunnlag(aar = 2023, type = DagpengetypeEnum.DP_FF),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).dagpengerFiskere shouldBe true
    }

    should("map foerstegangstjeneste") {
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

    should("map harUfoere") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = ufoeregrunnlag(type = UforetypeEnum.UF_M_YRKE),
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).harUfoere shouldBe true
    }

    should("filter out VIRK_IKKE_UFOR from uforeperioder") {
        SimulertOpptjeningMapper.simulertOpptjening(
            aar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = ufoeregrunnlag(type = UforetypeEnum.VIRK_IKKE_UFOR), // skal filtreres bort
            poengtallListe = emptyList(),
            useNullAsDefaultPensjonspoeng = true
        ).harUfoere shouldBe false
    }

    should("map harOffentligAfp") {
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

    should("return false for harOffentligAfp when afpHistorikkListe is empty") {
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
                val beregningsResultat = alderspensjonsresultat2025(pensjonsbeholdning = 4200000)

                SimulertOpptjeningMapper.simulertOpptjening(
                    aar = 2024,
                    resultatListe = listOf(beregningsResultat),
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
})

private val fom: LocalDate = LocalDate.of(2024, 1, 1)
private val tom: LocalDate = LocalDate.of(2024, 12, 31)

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

private fun alderspensjonsresultat2025(pensjonsbeholdning: Int) =
    BeregningsResultatAlderspensjon2025().apply {
        virkFomLd = fom
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