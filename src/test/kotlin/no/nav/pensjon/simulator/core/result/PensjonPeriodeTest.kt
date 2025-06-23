package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PensjonPeriodeTest : FunSpec({

    test("foerstePensjonsbeholdningFoerUttak should be første non-null 'pensjonBeholdningFoerUttak'") {
        PensjonPeriode().apply {
            simulertBeregningInformasjonListe = mutableListOf(
                SimulertBeregningInformasjon().apply { pensjonBeholdningFoerUttak = null },
                SimulertBeregningInformasjon().apply { pensjonBeholdningFoerUttak = 2 },
                SimulertBeregningInformasjon().apply { pensjonBeholdningFoerUttak = 3 },
                SimulertBeregningInformasjon().apply { pensjonBeholdningFoerUttak = 1 }
            )
        }.foerstePensjonsbeholdningFoerUttak shouldBe 2
    }
})
