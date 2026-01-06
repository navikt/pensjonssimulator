package no.nav.pensjon.simulator.tech.security.ingress.jwt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.security.oauth2.jwt.Jwt

class JwtAudienceValidatorTest : FunSpec({

    test("validate has no errors when audience is valid") {
        val validatorResult = JwtAudienceValidator(audience = "audience1").validate(jwt(audience = "audience1"))
        validatorResult.hasErrors() shouldBe false
    }

    test("validate has errors when audience is invalid") {
        val validatorResult = JwtAudienceValidator(audience = "audience1").validate(jwt(audience = "bad"))
        validatorResult.hasErrors() shouldBe true
    }

    test("validate has errors when audience claim is missing") {
        val validatorResult = JwtAudienceValidator(audience = "audience1").validate(jwt(claims = mapOf("x" to "y")))
        validatorResult.hasErrors() shouldBe true
    }
})

private fun jwt(audience: String) =
    jwt(claims = mapOf("aud" to audience))

private fun jwt(claims: Map<String, Any>) =
    Jwt("j.w.t", null, null, mapOf("k" to "v"), claims)
