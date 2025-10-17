package no.nav.pensjon.simulator.tech.security.ingress.claim

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.*

class ConsumerClaimUtilTest : FunSpec({

    val CORRECT_AUTHORITY = "iso6523-actorid-upis"
    val CORRECT_ICD_CODE = "0192"
    val WELL_FORMED_ID = "$CORRECT_ICD_CODE:123"

    test("should check that claim has an 'authority' key") {
        shouldThrow<RuntimeException> {
            ConsumerClaimUtil.organisasjonsnummer(consumerClaim = claim(authority = null, id = WELL_FORMED_ID))
        }.message shouldBe "Claim is missing 'authority' key"
    }

    test("should check that authority is as expected") {
        shouldThrow<RuntimeException> {
            ConsumerClaimUtil.organisasjonsnummer(consumerClaim = claim(authority = "x", id = WELL_FORMED_ID))
        }.message shouldBe "Unknown authority: 'x'"
    }

    test("should check that claim has an 'ID' key") {
        shouldThrow<RuntimeException> {
            ConsumerClaimUtil.organisasjonsnummer(consumerClaim = claim(authority = CORRECT_AUTHORITY, id = null))
        }.message shouldBe "Claim is missing 'ID' key"
    }

    test("should check that the ICD code is well-formed") {
        shouldThrow<RuntimeException> {
            ConsumerClaimUtil.organisasjonsnummer(
                consumerClaim = claim(authority = CORRECT_AUTHORITY, id = "192:123")
            )
        }.message shouldBe "Unknown ICD code in ID '192:123'"
    }

    test("should extract organisasjonsnummer when claim is well-formed") {
        ConsumerClaimUtil.organisasjonsnummer(
            consumerClaim = claim(authority = CORRECT_AUTHORITY, id = WELL_FORMED_ID)
        ) shouldBe "123"
    }
})

private fun claim(authority: String?, id: String?) =
    TreeMap<String, String>().apply {
        authority?.let { this["authority"] = it }
        id?.let { this["ID"] = it }
    }