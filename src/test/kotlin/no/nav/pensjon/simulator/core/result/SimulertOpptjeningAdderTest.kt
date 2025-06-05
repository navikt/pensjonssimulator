package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengrekke
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtMidnight
import java.time.LocalDate
import java.util.Calendar

class SimulertOpptjeningAdderTest : FunSpec({

    /**
     * addToOpptjeningListe should:
     * Bevare eksisterende opptjening-elementer
     * Legge til nye opptjening-elementer for kalenderårene fra første opptjeningsgrunnlag til året for øvre aldersgrense
     * Hente pensjonspoeng fra siste alderspensjon-beregningsresultat
     */
    test("addToOpptjeningListe") {
        val opptjeningListe = mutableListOf(SimulertOpptjening(kalenderAar = 2025)) // 1 initial opptjening element

        SimulertOpptjeningAdder(
            normalderService = mockk<NormertPensjonsalderService>().apply {
                every { oevreAlderOppnaasDato(any()) } returns LocalDate.of(2029, 1, 1) // => sisteKalenderAar = 2029
            }
        ).addToOpptjeningListe(
            opptjeningListe = opptjeningListe,
            beregningsresultatListe = mutableListOf(
                beregningsresultat2016(kalenderAar = 2027, pensjonspoeng = 1.23), // siste beregningsresultat
                beregningsresultat2016(kalenderAar = 2026, pensjonspoeng = 2.34), // ikke siste beregningsresultat
            ),
            soekerGrunnlag = Persongrunnlag().apply {
                fodselsdato = dateAtMidnight(1960, Calendar.JANUARY, 1)
                opptjeningsgrunnlagListe = mutableListOf(
                    Opptjeningsgrunnlag().apply { ar = 2027 },
                    Opptjeningsgrunnlag().apply { ar = 2026 } // => foersteKalenderAar = 2026
                )
            },
            regelverkType = RegelverkTypeEnum.N_REG_G_N_OPPTJ // => bruk BeregningsResultatAlderspensjon2016
        )

        opptjeningListe.size shouldBe 5
        opptjeningListe[0].kalenderAar shouldBe 2025 // => bevart eksisterende opptjening-element
        with(opptjeningListe[1]) {
            kalenderAar shouldBe 2026
            pensjonsgivendeInntektPensjonspoeng shouldBe 0.0 // pga. ikke siste beregningsresultat
        }
         with(opptjeningListe[2]) {
            kalenderAar shouldBe 2027
            pensjonsgivendeInntektPensjonspoeng shouldBe 1.23 // => hentet pensjonspoeng fra siste beregningsresultat
        }
        opptjeningListe[3].kalenderAar shouldBe 2028
        opptjeningListe[4].kalenderAar shouldBe 2029
    }
})

private fun beregningsresultat2016(kalenderAar: Int, pensjonspoeng: Double) =
    BeregningsResultatAlderspensjon2016().apply {
        virkFom = dateAtMidnight(kalenderAar, Calendar.JANUARY, 1)
        beregningsResultat2011 = BeregningsResultatAlderspensjon2011().apply {
            beregningsInformasjonKapittel19 = BeregningsInformasjon().apply {
                spt = Sluttpoengtall().apply {
                    poengrekke = Poengrekke().apply {
                        poengtallListe = mutableListOf(Poengtall().apply {
                            ar = kalenderAar
                            pp = pensjonspoeng
                        })
                    }
                }
            }
        }
    }
