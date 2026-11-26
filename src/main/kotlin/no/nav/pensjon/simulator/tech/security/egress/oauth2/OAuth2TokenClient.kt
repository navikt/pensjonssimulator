package no.nav.pensjon.simulator.tech.security.egress.oauth2

import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.metric.MetricResult
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.TokenData
import no.nav.pensjon.simulator.tech.security.egress.token.TokenDataGetter
import no.nav.pensjon.simulator.tech.security.egress.token.validation.ExpirationChecker
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

abstract class OAuth2TokenClient(
    private val tokenEndpoint: String,
    webClientBase: WebClientBase,
    retryAttempts: String,
    private val expirationChecker: ExpirationChecker
) : ExternalServiceClient(retryAttempts), TokenDataGetter {

    private val webClient = webClientBase.withBaseUrl(tokenEndpoint)

    override fun service() = service

    override fun getTokenData(accessParameter: TokenAccessParameter, audience: String): TokenData =
        try {
            val body: OAuth2TokenDto = webClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(prepareTokenRequestBody(accessParameter, audience))
                .retrieve()
                .bodyToMono<OAuth2TokenDto>()
                .retryWhen(retryBackoffSpec(tokenEndpoint))
                .block()!!
                .also { countCalls(MetricResult.OK) }
            // Note: Do not use .body instead of .bodyValue, since this results in chunked encoding,
            // which the endpoint may not support, resulting in 404 Not Found

            OAuth2TokenDataMapper.map(body, expirationChecker.time())
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $tokenEndpoint", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    protected fun isExpired(token: TokenData): Boolean =
        expirationChecker.isExpired(token.issuedTime, token.expiresInSeconds)

    protected abstract fun prepareTokenRequestBody(
        accessParameter: TokenAccessParameter, audience: String
    ): MultiValueMap<String, String>

    private companion object {
        private val service = EgressService.OAUTH2_TOKEN
    }
}