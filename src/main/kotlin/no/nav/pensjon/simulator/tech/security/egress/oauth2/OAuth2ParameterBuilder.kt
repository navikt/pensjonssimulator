package no.nav.pensjon.simulator.tech.security.egress.oauth2

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.ASSERTION
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.CLIENT_ID
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.CLIENT_SECRET
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterNames.GRANT_TYPE
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.*
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class OAuth2ParameterBuilder {

    private lateinit var accessParameter: TokenAccessParameter
    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var tokenAudience: String
    private lateinit var tokenRequestAudience: String
    private lateinit var assertion: String
    private lateinit var jwk: RSAKey

    fun tokenAccessParameter(value: TokenAccessParameter): OAuth2ParameterBuilder =
        this.also { accessParameter = value }

    fun clientId(value: String): OAuth2ParameterBuilder = this.also { clientId = value }

    fun clientSecret(value: String): OAuth2ParameterBuilder = this.also { clientSecret = value }

    fun tokenAudience(value: String) = this.also { tokenAudience = value }

    fun tokenRequestAudience(value: String) = this.also { tokenRequestAudience = value }

    fun assertion(value: String): OAuth2ParameterBuilder = this.also { assertion = value }

    fun jwk(value: RSAKey) = this.also { jwk = value }

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
            add(CLIENT_ASSERTION, createAssertion())
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

    private fun createAssertion(): String =
        SignedJWT(header(), getClaims()).apply {
            sign(RSASSASigner(jwk))
        }.serialize()

    private fun header() =
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(jwk.keyID)
            .build()

    private fun getClaims() =
        JWTClaimsSet.Builder()
            .issuer(clientId)
            .audience(tokenRequestAudience)
            .expirationTime(expirationTime())
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date())
            .notBeforeTime(Date())
            .subject(clientId)
            .claim(SCOPE, OidcScopes.OPENID)
            .build()

    private companion object {
        private const val EXPIRATION_TIME_MINUTES_IN_THE_FUTURE = 2

        private fun expirationTime(): Date? =
            fromLocalDateTime(
                LocalDateTime.now()
                    .plus(Duration.ofMinutes(EXPIRATION_TIME_MINUTES_IN_THE_FUTURE.toLong()))
            )

        private fun fromLocalDateTime(dateTime: LocalDateTime) =
            Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
    }
}