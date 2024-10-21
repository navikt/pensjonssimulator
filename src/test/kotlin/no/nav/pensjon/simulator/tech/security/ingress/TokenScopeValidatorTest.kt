package no.nav.pensjon.simulator.tech.security.ingress

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.security.oauth2.jwt.Jwt

class TokenScopeValidatorTest : FunSpec({

    test("validate has no errors when scope is valid") {
        val validatorResult = TokenScopeValidator(scope = "scope1").validate(jwt(scope = "scope1"))
        validatorResult.hasErrors() shouldBe false
    }

    test("validate has errors when scope is invalid") {
        val validatorResult = TokenScopeValidator(scope = "scope1").validate(jwt(scope = "bad"))
        validatorResult.hasErrors() shouldBe true
    }

    test("validate has errors when scope claim is missing") {
        val validatorResult = TokenScopeValidator(scope = "scope1").validate(jwt(claims = mapOf("x" to "y")))
        validatorResult.hasErrors() shouldBe true
    }
})

private fun jwt(scope: String) =
    jwt(claims = mapOf("scope" to scope))

private fun jwt(claims: Map<String, Any>) =
    Jwt("j.w.t", null, null, mapOf("k" to "v"), claims)
