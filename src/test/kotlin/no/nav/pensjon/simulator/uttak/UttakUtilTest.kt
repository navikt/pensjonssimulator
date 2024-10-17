package no.nav.pensjon.simulator.uttak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.krav.UttakGradKode

class UttakUtilTest : FunSpec({

    test("indexedUttakGradSubmap for 80 prosent inkluderer alle uttaksgrader") {
        val map = UttakUtil.indexedUttakGradSubmap(UttakGradKode.P_80)

        map.size shouldBe 5
        map[0] shouldBe UttakGradKode.P_80
        map[1] shouldBe UttakGradKode.P_60
        map[2] shouldBe UttakGradKode.P_50
        map[3] shouldBe UttakGradKode.P_40
        map[4] shouldBe UttakGradKode.P_20
    }

    test("indexedUttakGradSubmap for 50 prosent inkluderer uttaksgradene 50, 40 og 20 prosent") {
        val map = UttakUtil.indexedUttakGradSubmap(UttakGradKode.P_50)

        map.size shouldBe 3
        map[0] shouldBe UttakGradKode.P_50
        map[1] shouldBe UttakGradKode.P_40
        map[2] shouldBe UttakGradKode.P_20
    }

    test("indexedUttakGradSubmap for 20 prosent inkluderer bare uttaksgraden 20 prosent") {
        val map = UttakUtil.indexedUttakGradSubmap(UttakGradKode.P_20)

        map.size shouldBe 1
        map[0] shouldBe UttakGradKode.P_20
    }
})
