package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.validity.ProblemType

class Pre2025TpV3ProblemTypeTest : ShouldSpec({

    context("funksjonen 'from'") {
        should("mappe fra internverdi til eksternverdi") {
            Pre2025TpV3ProblemType.from(
                internalValue = ProblemType.UTILSTREKKELIG_TRYGDETID
            ) shouldBe Pre2025TpV3ProblemType.UTILSTREKKELIG_TRYGDETID
        }
    }
})