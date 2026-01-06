package no.nav.pensjon.simulator.tech.security.ingress

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class MaskinportenAuthenticationConverterTest : FunSpec({

    val converter = MaskinportenAuthenticationConverter()

    test("should convert claims in well-formed JWT to authentication token with authorities") {
        val jwt = jwtBase()
            .claim(
                "consumer",
                mapOf(
                    "authority" to "iso6523-actorid-upis",
                    "ID" to "0192:123456789"
                )
            )
            .claim("scope", "nav:pensjonssimulator:simulering")
            .build()

        val authToken: AbstractAuthenticationToken = converter.convert(token = jwt)

        with(authToken) {
            name shouldBe "123456789"
            authorities shouldHaveSize 2
            with(authorities.filterIsInstance<ConsumerOrganizationGrantedAuthority>().first()) {
                authority shouldBe "consumer-organisation:123456789"
                organisasjonsnummer shouldBe "123456789"
            }
            authorities.filterIsInstance<SimpleGrantedAuthority>()
                .first().authority shouldBe "scope:simuler-alderspensjon"
        }
    }

    test("should throw exception if JWT lacks 'consumer' claim") {
        val jwt = jwtBase()
            .claim("scope", "nav:pensjonssimulator:simulering")
            .build()

        shouldThrow<AuthenticationException> {
            converter.convert(token = jwt)
        }.message shouldBe "Maskinporten-token mangler 'consumer' claim"
    }
})

private fun jwtBase() =
    Jwt
        .withTokenValue("foo")
        .header("typ", "JWT")
