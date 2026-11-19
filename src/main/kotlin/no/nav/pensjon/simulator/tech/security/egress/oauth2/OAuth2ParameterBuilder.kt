package no.nav.pensjon.simulator.tech.security.egress.oauth2

import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.ASSERTION
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.CLIENT_ID
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.CLIENT_SECRET
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.GRANT_TYPE
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class OAuth2ParameterBuilder private constructor(private val accessParameter: TokenAccessParameter) {

    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var tokenAudience: String
    private lateinit var tokenRequestAudience: String
    private lateinit var assertion: String
    private lateinit var assertionCreator: AssertionCreator
    private lateinit var jwk: String

    fun clientId(value: String) = this.also { clientId = value }

    fun clientSecret(value: String) = this.also { clientSecret = value }

    fun tokenAudience(value: String) = this.also { tokenAudience = value }

    fun tokenRequestAudience(value: String) = this.also { tokenRequestAudience = value }

    fun assertion(value: String) = this.also { assertion = value }

    fun assertionCreator(value: AssertionCreator) = this.also { assertionCreator = value }

    fun jwk(value: String) = this.also { jwk = value }

    fun buildClientCredentialsTokenRequestMap(): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add(GRANT_TYPE, accessParameter.getGrantTypeName())
            add(accessParameter.getParameterName(), accessParameter.value)
            add(CLIENT_ID, clientId)
            add(CLIENT_SECRET, clientSecret)
        }

    fun buildOnBehalfOfTokenRequestMap(): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add(OAuth2ParameterNames.GRANT_TYPE, accessParameter.getGrantTypeName())
            add(accessParameter.getParameterName(), accessParameter.value)
            add(OAuth2ParameterNames.CLIENT_ID, clientId)
            add(OAuth2ParameterNames.CLIENT_SECRET, clientSecret)
            add(SCOPE, tokenAudience)
            add("requested_token_use", "on_behalf_of")
        }

    fun tokenExchangeRequestMap(): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add(OAuth2ParameterNames.GRANT_TYPE, accessParameter.getGrantTypeName())
            add(accessParameter.getParameterName(), accessParameter.value)
            add(CLIENT_ASSERTION_TYPE, "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            add(CLIENT_ASSERTION, assertionCreator.createAssertion(jwk, clientId, tokenRequestAudience))
            add(AUDIENCE, tokenAudience)
            add(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:jwt")
            add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        }

    fun buildMaskinportenTokenRequestMap(): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add(GRANT_TYPE, accessParameter.getGrantTypeName())
            add(ASSERTION, assertion)
        }

    companion object {
        fun tokenAccessParameter(value: TokenAccessParameter) =
            OAuth2ParameterBuilder(accessParameter = value)
    }
}