package no.nav.pensjon.simulator.tech.sporing

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant

class SecurityContextOrganisasjonsnummerExtractorTest : FunSpec({

    val extractor = SecurityContextOrganisasjonsnummerExtractor()

    afterTest {
        SecurityContextHolder.clearContext()
    }

    context("provideOrganisasjonsnummer") {

        test("returnerer organisasjonsnummer fra security context") {
            val orgnr = "123456789"
            setupSecurityContextWithConsumerClaim(orgnr)

            val result = extractor.provideOrganisasjonsnummer()

            result.value shouldBe orgnr
        }

        test("kaster RuntimeException når security context ikke har authentication") {
            SecurityContextHolder.clearContext()

            val exception = shouldThrow<RuntimeException> {
                extractor.provideOrganisasjonsnummer()
            }

            exception.message shouldContain "Organisasjonsnummer not found"
        }

        test("kaster RuntimeException når authentication ikke har credentials") {
            val securityContext = object : SecurityContext {
                override fun getAuthentication(): Authentication = object : Authentication {
                    override fun getName(): String = "test"
                    override fun getAuthorities() = emptyList<Nothing>()
                    override fun getCredentials(): Any? = null
                    override fun getDetails(): Any? = null
                    override fun getPrincipal(): Any = "principal"
                    override fun isAuthenticated(): Boolean = true
                    override fun setAuthenticated(isAuthenticated: Boolean) {}
                }
                override fun setAuthentication(authentication: Authentication?) {}
            }
            SecurityContextHolder.setContext(securityContext)

            val exception = shouldThrow<RuntimeException> {
                extractor.provideOrganisasjonsnummer()
            }

            exception.message shouldContain "Organisasjonsnummer not found"
        }

        test("kaster RuntimeException når JWT ikke har consumer claim") {
            val jwt = createJwtWithClaims(emptyMap())
            setupSecurityContextWithJwt(jwt)

            val exception = shouldThrow<RuntimeException> {
                extractor.provideOrganisasjonsnummer()
            }

            exception.message shouldContain "Organisasjonsnummer not found"
        }

        test("kaster exception når consumer claim mangler authority") {
            val consumerClaim = mapOf(
                "ID" to "0192:123456789"
            )
            val jwt = createJwtWithClaims(mapOf("consumer" to consumerClaim))
            setupSecurityContextWithJwt(jwt)

            shouldThrow<Exception> {
                extractor.provideOrganisasjonsnummer()
            }
        }

        test("kaster exception når consumer claim har feil authority") {
            val consumerClaim = mapOf(
                "authority" to "wrong-authority",
                "ID" to "0192:123456789"
            )
            val jwt = createJwtWithClaims(mapOf("consumer" to consumerClaim))
            setupSecurityContextWithJwt(jwt)

            shouldThrow<Exception> {
                extractor.provideOrganisasjonsnummer()
            }
        }

        test("kaster exception når consumer claim mangler ID") {
            val consumerClaim = mapOf(
                "authority" to "iso6523-actorid-upis"
            )
            val jwt = createJwtWithClaims(mapOf("consumer" to consumerClaim))
            setupSecurityContextWithJwt(jwt)

            shouldThrow<Exception> {
                extractor.provideOrganisasjonsnummer()
            }
        }

        test("kaster exception når ID har feil ICD code prefix") {
            val consumerClaim = mapOf(
                "authority" to "iso6523-actorid-upis",
                "ID" to "9999:123456789"
            )
            val jwt = createJwtWithClaims(mapOf("consumer" to consumerClaim))
            setupSecurityContextWithJwt(jwt)

            shouldThrow<Exception> {
                extractor.provideOrganisasjonsnummer()
            }
        }

        test("håndterer ulike gyldige organisasjonsnumre") {
            val testOrgnrs = listOf("987654321", "111111111", "999999999")

            testOrgnrs.forEach { orgnr ->
                setupSecurityContextWithConsumerClaim(orgnr)
                val result = extractor.provideOrganisasjonsnummer()
                result.value shouldBe orgnr
            }
        }
    }
})

private fun setupSecurityContextWithConsumerClaim(organisasjonsnummer: String) {
    val consumerClaim = mapOf(
        "authority" to "iso6523-actorid-upis",
        "ID" to "0192:$organisasjonsnummer"
    )
    val jwt = createJwtWithClaims(mapOf("consumer" to consumerClaim))
    setupSecurityContextWithJwt(jwt)
}

private fun createJwtWithClaims(claims: Map<String, Any>): Jwt {
    return Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claims { it.putAll(claims) }
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build()
}

private fun setupSecurityContextWithJwt(jwt: Jwt) {
    val authentication = object : Authentication {
        override fun getName(): String = "test"
        override fun getAuthorities() = emptyList<Nothing>()
        override fun getCredentials(): Any = jwt
        override fun getDetails(): Any? = null
        override fun getPrincipal(): Any = "principal"
        override fun isAuthenticated(): Boolean = true
        override fun setAuthenticated(isAuthenticated: Boolean) {}
    }

    val securityContext = object : SecurityContext {
        override fun getAuthentication(): Authentication = authentication
        override fun setAuthentication(authentication: Authentication?) {}
    }

    SecurityContextHolder.setContext(securityContext)
}
