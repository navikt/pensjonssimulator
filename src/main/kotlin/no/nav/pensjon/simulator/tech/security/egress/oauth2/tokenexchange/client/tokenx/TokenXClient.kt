package no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.client.tokenx

import com.nimbusds.jose.jwk.RSAKey
import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterBuilder
import no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.TokenExchangeCredentials
import no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.client.TokenExchangeClient
import no.nav.pensjon.simulator.tech.security.egress.token.CacheAwareTokenClient
import no.nav.pensjon.simulator.tech.security.egress.token.CacheKey
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.validation.ExpirationChecker
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap

@Component
class TokenXClient(
    @param:Value($$"${token-x.token.endpoint}") private val tokenEndpoint: String,
    webClientBase: WebClientBase,
    expirationChecker: ExpirationChecker,
    private val credentials: TokenExchangeCredentials,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String
) : CacheAwareTokenClient(
    tokenEndpoint,
    webClientBase,
    retryAttempts,
    expirationChecker
), TokenExchangeClient {

    override fun prepareTokenRequestBody(
        accessParameter: TokenAccessParameter,
        audience: String // format: "cluster:namespace:app"
    ): MultiValueMap<String, String> =
        OAuth2ParameterBuilder()
            .tokenAccessParameter(accessParameter)
            .clientId(credentials.clientId)
            .tokenAudience(audience)
            .tokenRequestAudience(tokenEndpoint)
            .jwk(RSAKey.parse(credentials.jwk))
            .tokenExchangeRequestMap()

    override fun exchange(accessParameter: TokenAccessParameter, cacheKey: CacheKey) =
        getTokenData(accessParameter, scope = cacheKey.scope, user = cacheKey.pid.value)
}