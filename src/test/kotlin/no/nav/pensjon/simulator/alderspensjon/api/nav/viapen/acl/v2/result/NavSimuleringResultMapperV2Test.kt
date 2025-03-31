package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengrekke
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertOpptjening

class NavSimuleringResultMapperV2Test : FunSpec({

    test("toSimuleringResultV2 should use 0.0 as default for pensjonspoengPi") {
        NavSimuleringResultMapperV2.toSimuleringResultV2(
            SimulatorOutput().apply {
                opptjeningListe.add(SimulertOpptjening(pensjonsgivendeInntektPensjonspoeng = null))
            }
        ).opptjeningListe[0].pensjonspoengPi shouldBe 0.0
    }

    test("toSimuleringResultV2 should use aar from poengtall as merknad aar") {
        NavSimuleringResultMapperV2.toSimuleringResultV2(
            SimulatorOutput().apply {
                pre2025OffentligAfp = Simuleringsresultat().apply {
                    beregning = Beregning().apply {
                        tp = Tilleggspensjon().apply {
                            spt = Sluttpoengtall().apply {
                                poengrekke = Poengrekke().apply {
                                    poengtallListe = mutableListOf(
                                        Poengtall().apply {
                                            ar = 2025
                                            merknadListe = mutableListOf(Merknad())
                                        })
                                }
                            }
                        }
                    }
                }
            }
        ).afpOffentlig?.beregning?.ytelseskomponenter[0]
            ?.spt?.poengrekke?.poengtallListe[0]?.merknadListe[0]?.ar shouldBe 2025
    }
})
