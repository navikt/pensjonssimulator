package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PensjonPeriodeTest : FunSpec({

    test("latestBeregningInformasjon should pick element with highest startMaaned") {
        PensjonPeriode().apply {
            simulertBeregningInformasjonListe = mutableListOf(
                SimulertBeregningInformasjon().apply {
                    startMaaned = 2
                    aarligBeloep = 200
                },
                SimulertBeregningInformasjon().apply {
                    startMaaned = 1
                    aarligBeloep = 100
                },
                SimulertBeregningInformasjon().apply {
                    startMaaned = 3
                    aarligBeloep = 300
                },
                SimulertBeregningInformasjon().apply {
                    startMaaned = null
                    aarligBeloep = 0
                })
        }.latestBeregningInformasjon?.aarligBeloep shouldBe 300
    }

    test("if all startMaaned undefined then latestBeregningInformasjon should pick first element") {
        PensjonPeriode().apply {
            simulertBeregningInformasjonListe = mutableListOf(
                SimulertBeregningInformasjon().apply {
                    startMaaned = null
                    aarligBeloep = 100
                },
                SimulertBeregningInformasjon().apply {
                    startMaaned = null
                    aarligBeloep = 200
                })
        }.latestBeregningInformasjon?.aarligBeloep shouldBe 100
    }

    test("if empty list then latestBeregningInformasjon should return null") {
        PensjonPeriode().apply {
            simulertBeregningInformasjonListe = mutableListOf()
        }.latestBeregningInformasjon shouldBe null
    }
})
