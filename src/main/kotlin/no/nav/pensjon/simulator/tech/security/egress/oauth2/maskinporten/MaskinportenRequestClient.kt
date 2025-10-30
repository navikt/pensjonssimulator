package no.nav.pensjon.simulator.tech.security.egress.oauth2.maskinporten

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterBuilder
import no.nav.pensjon.simulator.tech.security.egress.token.CacheAwareTokenClient
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.validation.ExpirationChecker
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import java.util.*

@Component
class MaskinportenRequestClient(
    @param:Value("\${maskinporten.token-endpoint-url}") val tokenEndpoint: String,
    webClientBase: WebClientBase,
    expirationChecker: ExpirationChecker,
    private val credentials: MaskinportenCredentials,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String
) : CacheAwareTokenClient(
    tokenEndpoint,
    webClientBase,
    retryAttempts,
    expirationChecker
) {
    override fun prepareTokenRequestBody(
        accessParameter: TokenAccessParameter,
        audience: String
    ): MultiValueMap<String, String> =
        OAuth2ParameterBuilder()
            .tokenAccessParameter(accessParameter)
            .assertion(signedJwt(scope = audience, issueTime = Date()))
            .buildMaskinportenTokenRequestMap()

    private fun signedJwt(scope: String, issueTime: Date): String =
        SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(credentials.keyId)
                .type(JOSEObjectType.JWT)
                .build(),
            JWTClaimsSet.Builder()
                .audience(credentials.issuer)
                .issuer(credentials.clientId)
                .claim("scope", scope)
                .issueTime(issueTime)
                .expirationTime(expirationTime(issueTime))
                .build()
        ).also {
            it.sign(RSASSASigner(credentials.privateKey))
        }.serialize()

    private fun expirationTime(issueTime: Date): Date =
        Calendar.getInstance().apply {
            time = issueTime
            add(Calendar.SECOND, EXPIRE_AFTER_SECONDS)
        }.time

    companion object {
        private const val EXPIRE_AFTER_SECONDS: Int = 70
    }
}
