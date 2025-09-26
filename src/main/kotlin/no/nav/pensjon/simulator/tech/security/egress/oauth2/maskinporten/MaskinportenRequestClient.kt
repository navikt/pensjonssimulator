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
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Component
class MaskinportenRequestClient(
    @param:Value("\${maskinporten.token-endpoint-url}") val tokenEndpoint: String,
    webClientBuilder: WebClient.Builder,
    expirationChecker: ExpirationChecker,
    private val credentials: MaskinportenCredentials,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String
) : CacheAwareTokenClient(
    tokenEndpoint,
    webClientBuilder,
    retryAttempts,
    expirationChecker
) {

    override fun prepareTokenRequestBody(
        accessParameter: TokenAccessParameter,
        audience: String
    ): MultiValueMap<String, String> {
        return OAuth2ParameterBuilder()
            .tokenAccessParameter(accessParameter)
            .assertion(signJwt(audience))
            .buildMaskinportenTokenRequestMap()
    }

    private fun signJwt(scope: String): String {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(credentials.keyId)
                .type(JOSEObjectType.JWT)
                .build(),
            JWTClaimsSet.Builder()
                .audience(credentials.issuer)
                .issuer(credentials.clientId)
                .claim("scope", scope)
                .issueTime(Date())
                .expirationTime(getExpireAfter())
                .build()
        )
        signedJWT.sign(RSASSASigner(credentials.privateKey))
        return signedJWT.serialize()
    }

    private fun getExpireAfter(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = Date();
        calendar.add(Calendar.SECOND, EXPIRE_AFTER_SECONDS)
        return calendar.time
    }

    companion object {
        private const val EXPIRE_AFTER_SECONDS: Int = 70
    }
}
