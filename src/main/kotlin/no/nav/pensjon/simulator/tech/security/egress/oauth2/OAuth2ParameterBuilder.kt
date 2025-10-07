package no.nav.pensjon.simulator.tech.security.egress.oauth2

import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.ASSERTION
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.CLIENT_ID
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.CLIENT_SECRET
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.GRANT_TYPE
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class OAuth2ParameterBuilder {

    private lateinit var accessParameter: TokenAccessParameter
    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var assertion: String

    fun tokenAccessParameter(value: TokenAccessParameter): OAuth2ParameterBuilder =
        this.also { accessParameter = value }

    fun clientId(value: String): OAuth2ParameterBuilder = this.also { clientId = value }

    fun clientSecret(value: String): OAuth2ParameterBuilder = this.also { clientSecret = value }

    fun assertion(value: String): OAuth2ParameterBuilder = this.also { assertion = value }

    fun buildClientCredentialsTokenRequestMap(): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add(GRANT_TYPE, accessParameter.getGrantTypeName())
            add(accessParameter.getParameterName(), accessParameter.value)
            add(CLIENT_ID, clientId)
            add(CLIENT_SECRET, clientSecret)
        }

    fun buildMaskinportenTokenRequestMap(): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add(GRANT_TYPE, accessParameter.getGrantTypeName())
            add(ASSERTION, assertion)
        }
}
