package no.nav.pensjon.simulator.tech.security.egress.oauth2.clientcred

import no.nav.pensjon.simulator.tech.security.egress.oauth2.OAuth2ParameterBuilder
import no.nav.pensjon.simulator.tech.security.egress.token.CacheAwareTokenClient
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.validation.ExpirationChecker
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap

@Component
class ClientCredentialsTokenRequestClient(
    @Value($$"${azure.openid-config.token-endpoint}") tokenEndpoint: String,
    webClientBase: WebClientBase,
    expirationChecker: ExpirationChecker,
    private val credentials: ClientCredentials,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String
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
            .clientId(credentials.clientId)
            .clientSecret(credentials.clientSecret)
            .buildClientCredentialsTokenRequestMap()
}
