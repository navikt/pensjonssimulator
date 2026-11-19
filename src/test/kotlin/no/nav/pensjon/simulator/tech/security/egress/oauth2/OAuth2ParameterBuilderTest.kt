package no.nav.pensjon.simulator.tech.security.egress.oauth2

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import org.springframework.util.MultiValueMap

class OAuth2ParameterBuilderTest : ShouldSpec({

    context("funksjonen 'buildClientCredentialsTokenRequestMap'") {
        should("build map with client credentials parameters") {
            val map = OAuth2ParameterBuilder
                .tokenAccessParameter(TokenAccessParameter.clientCredentials("scope1"))
                .clientId("id1")
                .clientSecret("secret1")
                .buildClientCredentialsTokenRequestMap()

            value(map, key = "grant_type") shouldBe "client_credentials"
            value(map, key = "scope") shouldBe "scope1"
            value(map, key = "client_id") shouldBe "id1"
            value(map, key = "client_secret") shouldBe "secret1"
        }
    }

    context("funksjonen 'tokenExchangeRequestMap'") {
        should("build map with TokenX parameters") {
            val map = OAuth2ParameterBuilder
                .tokenAccessParameter(TokenAccessParameter.tokenExchange(subjectToken = "j.w.t"))
                .clientId("id1")
                .jwk("j-w-k")
                .tokenAudience("aud1")
                .tokenRequestAudience("aud2")
                .assertionCreator(arrangeAssertion()) // "created-assertion"
                .tokenExchangeRequestMap()

            value(map, key = "grant_type") shouldBe "urn:ietf:params:oauth:grant-type:token-exchange"
            value(map, key = "subject_token") shouldBe "j.w.t"
            value(map, key = "client_assertion_type") shouldBe "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
            value(map, key = "client_assertion") shouldBe "created-assertion"
            value(map, key = "audience") shouldBe "aud1"
            value(map, key = "subject_token_type") shouldBe "urn:ietf:params:oauth:token-type:jwt"
        }
    }

    context("funksjonen 'buildMaskinportenTokenRequestMap'") {
        should("build map with Maskinporten parameters") {
            val map = OAuth2ParameterBuilder
                .tokenAccessParameter(TokenAccessParameter.jwtBearer(assertion = "ignored-assertion"))
                .assertion("assertion1")
                .buildMaskinportenTokenRequestMap()

            value(map, key = "grant_type") shouldBe "urn:ietf:params:oauth:grant-type:jwt-bearer"
            value(map, key = "assertion") shouldBe "assertion1"
        }
    }
})

private fun arrangeAssertion(): AssertionCreator =
    mockk { every { createAssertion(any(), any(), any()) } returns "created-assertion" }

private fun value(map: MultiValueMap<String, String>, key: String): String =
    map[key]!![0]