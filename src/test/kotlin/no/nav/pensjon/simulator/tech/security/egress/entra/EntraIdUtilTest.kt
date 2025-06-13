package no.nav.pensjon.simulator.tech.security.egress.entra

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EntraIdUtilTest : FunSpec({

    test("'getDefaultScope' returns the default scope for a given service") {
        EntraIdUtil.getDefaultScope("cluster1:namespace1:app1") shouldBe "api://cluster1.namespace1.app1/.default"
    }
})
