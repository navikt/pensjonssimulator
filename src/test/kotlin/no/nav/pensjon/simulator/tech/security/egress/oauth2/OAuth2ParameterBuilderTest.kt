package no.nav.pensjon.simulator.tech.security.egress.oauth2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter

class OAuth2ParameterBuilderTest : FunSpec({

    test("buildClientCredentialsTokenRequestMap should build map with client credentials parameters") {
        val map = OAuth2ParameterBuilder()
            .clientId("id1")
            .clientSecret("secret1")
            .tokenAccessParameter(TokenAccessParameter.clientCredentials("scope1"))
            .buildClientCredentialsTokenRequestMap()

        map["grant_type"]!![0] shouldBe "client_credentials"
        map["scope"]!![0] shouldBe "scope1"
        map["client_id"]!![0] shouldBe "id1"
        map["client_secret"]!![0] shouldBe "secret1"
    }
})
