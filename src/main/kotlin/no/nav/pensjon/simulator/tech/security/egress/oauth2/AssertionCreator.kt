package no.nav.pensjon.simulator.tech.security.egress.oauth2

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class AssertionCreator {

    fun createAssertion(jwk: String, clientId: String, audience: String): String =
        RSAKey.parse(jwk).let {
            SignedJWT(header(it.keyID), getClaims(clientId, audience)).apply {
                sign(RSASSASigner(it))
            }.serialize()
        }

    private fun header(keyId: String) =
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(keyId)
            .build()

    private fun getClaims(clientId: String, audience: String) =
        Date().let {
            JWTClaimsSet.Builder()
                .issuer(clientId)
                .audience(audience)
                .expirationTime(expirationTime())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(it)
                .notBeforeTime(it)
                .subject(clientId)
                .claim(SCOPE, OidcScopes.OPENID)
                .build()
        }

    private companion object {
        private const val EXPIRATION_TIME_MINUTES_IN_THE_FUTURE = 2

        private fun expirationTime(): Date =
            date(
                LocalDateTime.now()
                    .plus(Duration.ofMinutes(EXPIRATION_TIME_MINUTES_IN_THE_FUTURE.toLong()))
            )

        private fun date(dateTime: LocalDateTime) =
            Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
    }
}