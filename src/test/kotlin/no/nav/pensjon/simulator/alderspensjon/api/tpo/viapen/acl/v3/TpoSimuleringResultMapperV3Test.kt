package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import java.time.LocalDate

class TpoSimuleringResultMapperV3Test : FunSpec({

    test("toDto maps SimulertBeregningInformasjon") {
        val actual = TpoSimuleringResultMapperV3.toDto(
            SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    pensjonBeholdningListe = listOf(
                        BeholdningPeriode(
                            datoFom = LocalDate.of(2024, 5, 6),
                            pensjonsbeholdning = null,
                            garantipensjonsbeholdning = null,
                            garantitilleggsbeholdning = null,
                            garantipensjonsniva = null
                        )
                    )
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply {
                            datoFom = LocalDate.of(2023, 4, 5)
                            aarligBeloep = 1
                            maanedligBeloep = 2
                            startMaaned = 3
                            vinnendeBeregning = GrunnlagRolle.SOKER
                            uttakGrad = 1.2
                            kapittel20Pensjon = 4
                            vektetKapittel20Pensjon = 5
                            inntektspensjon = 6
                            garantipensjon = 7
                            garantitillegg = 8
                            pensjonBeholdningFoerUttak = 9
                            pensjonBeholdningEtterUttak = 10
                            kapittel19Pensjon = 11
                            vektetKapittel19Pensjon = 12
                            basispensjon = 13
                            basisGrunnpensjon = 1.3
                            basisTilleggspensjon = 1.4
                            basisPensjonstillegg = 1.5
                            restBasisPensjon = 14
                            grunnpensjon = 15
                            tilleggspensjon = 16
                            pensjonstillegg = 17
                            individueltMinstenivaaTillegg = 18
                            pensjonistParMinstenivaaTillegg = 19
                            skjermingstillegg = 20
                            ufoereGrad = 21
                            forholdstall = 1.6
                            delingstall = 1.7
                            tt_anv_kap19 = 22
                            apKap19medGJR = 23
                            apKap19utenGJR = 24
                            gjtAP = 25
                            gjtAPKap19 = 26
                            tt_anv_kap20 = 27
                            pa_f92 = 28
                            pa_e91 = 29
                            spt = 1.8
                            nOkap19 = 30
                            nOkap20 = 31
                            minstePensjonsnivaSats = 1.9
                        }
                    )
                }
            })

        with(actual.ap!!) {
            pensjonsbeholdningListe[0].datoFom shouldBe LocalDate.of(2024, 5, 6)
            with(simulertBeregningsinformasjonListe[0]) {
                datoFom shouldBe LocalDate.of(2023, 4, 5)
                uttaksgrad shouldBe 1.2
                gp shouldBe 15
                tp shouldBe 16
                pt shouldBe 17
                minstenivaTilleggIndividuelt shouldBe 18
                inntektspensjon shouldBe 6
                garantipensjon shouldBe 7
                garantitillegg shouldBe 8
                skjermt shouldBe 20
                pa_f92 shouldBe 28
                pa_e91 shouldBe 29
                spt shouldBe 1.8
                tt_anv_kap19 shouldBe 22
                basisgp shouldBe 1.3
                basistp shouldBe 1.4
                basispt shouldBe 1.5
                forholdstall shouldBe 1.6
                delingstall shouldBe 1.7
                ufg shouldBe 21
            }
        }
    }
})
