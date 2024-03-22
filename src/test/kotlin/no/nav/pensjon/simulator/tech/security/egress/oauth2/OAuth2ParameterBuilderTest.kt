package no.nav.pensjon.simulator.tech.security.egress.oauth2

import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.util.MultiValueMap

class OAuth2ParameterBuilderTest {

    @Test
    fun `buildClientCredentialsTokenRequestMap builds map with client credentials parameters`() {
        val map = OAuth2ParameterBuilder()
            .clientId("id1")
            .clientSecret("secret1")
            .tokenAccessParameter(TokenAccessParameter.clientCredentials("scope1"))
            .buildClientCredentialsTokenRequestMap()

        assertMapValue("client_credentials", map, "grant_type")
        assertMapValue("scope1", map, "scope")
        assertMapValue("id1", map, "client_id")
        assertMapValue("secret1", map, "client_secret")
    }

    companion object {
        private fun assertMapValue(expectedValue: String, map: MultiValueMap<String, String>, key: String) {
            assertEquals(expectedValue, map[key]!![0])
        }
    }
}
