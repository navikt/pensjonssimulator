package no.nav.pensjon.simulator.generelt.organisasjon

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OrganisasjonsnummerTest : FunSpec({

    test("toString returns the original organisasjonsnummer") {
        "Org: ${Organisasjonsnummer("123456789")}" shouldBe "Org: 123456789"
    }
})
