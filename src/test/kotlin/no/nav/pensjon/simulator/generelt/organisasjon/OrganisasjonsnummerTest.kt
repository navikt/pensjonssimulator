package no.nav.pensjon.simulator.generelt.organisasjon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OrganisasjonsnummerTest : FunSpec({

    test("toString should return the original organisasjonsnummer") {
        "Org: ${Organisasjonsnummer("123456789")}" shouldBe "Org: 123456789"
    }

    test("organisasjonsnummer should require length 9") {
        shouldThrow<IllegalArgumentException> {
            Organisasjonsnummer("")
        }.message shouldBe "Feil lengde (0) på organisasjonsnummer; må være 9"

        shouldThrow<IllegalArgumentException> {
            Organisasjonsnummer("1234567890")
        }.message shouldBe "Feil lengde (10) på organisasjonsnummer; må være 9"
    }
})
