package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon

class TpoSimuleringResultMapperV1Test : FunSpec({

    test("toDto maps SimulatorOutput to TpoSimuleringResultV1") {
        val result: TpoSimuleringResultV1 = TpoSimuleringResultMapperV1.toDto(
            SimulatorOutput().apply {
                privatAfpPeriodeListe.add(
                    PrivatAfpPeriode(
                        afpOpptjening = null,
                        alderAar = 65,
                        aarligBeloep = 100,
                        maanedligBeloep = null,
                        livsvarig = null,
                        kronetillegg = null,
                        kompensasjonstillegg = null,
                        afpForholdstall = null,
                        justeringBeloep = null
                    )
                )
                alderspensjon = SimulertAlderspensjon().apply {
                    addPensjonsperiode(
                        PensjonPeriode().apply {
                            alderAar = 66
                            beloep = 123
                            simulertBeregningInformasjonListe = mutableListOf(
                                SimulertBeregningInformasjon().apply {
                                    startMaaned = 3
                                    uttakGrad = 1.2
                                }
                            )
                        }
                    )
                }
            })

        with(result) {
            with(afpPrivat!![0]) {
                alder shouldBe 65
                belopArlig shouldBe 100
            }
            with(ap!!.pensjonsperiodeListe!![0]) {
                alder shouldBe 66
                belop shouldBe 123
                with(simulertBeregningsinformasjonListe!![0]) {
                    startMnd shouldBe 3
                    uttaksgrad shouldBe 1.2
                }
            }
        }
    }
})
