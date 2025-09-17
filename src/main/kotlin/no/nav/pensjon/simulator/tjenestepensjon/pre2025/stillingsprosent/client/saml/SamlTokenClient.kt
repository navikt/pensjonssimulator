package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class SamlTokenClient(
    @param:Value("\${ps.sts.url}") private val baseUrl: String,
    webClientBuilder: WebClient.Builder,
    ) {
    private val webClient: WebClient = webClientBuilder.baseUrl(baseUrl).build()
    private val log = KotlinLogging.logger {}

    var samlAccessToken: SamlToken = SamlToken("", expiresIn = -1)
        get() = if (field.isExpired) hentSamlToken().also { field = it }
        else field

    private fun hentSamlToken(): SamlToken {
        return try {
            val token = EgressAccess.token(EgressService.FSS_GATEWAY).value
            webClient
                .post()
                .uri(TOKEN_EXCHANGE_PATH)
                .headers {
                    it.setBearerAuth(token)
                    it["Service-User-Id"] = "3"
                }
                .body(body(token))
                .retrieve()
                .bodyToMono(SamlToken::class.java)
                .block()
                .also { log.info { "Hentet SAML token fra fss-gateway" } } ?: throw RuntimeException("Failed to fetch SAML token from fss-gateway")
        } catch (e: WebClientRequestException) {
            log.error(e) { "Failed to fetch SAML, WebClientRequestException" }
            throw RuntimeException(DEFAULT_ERROR_MSG, e)
        } catch (e: WebClientResponseException) {
            log.error(e) { "Failed to fetch SAML, WebClientResponseException" }
            throw RuntimeException(DEFAULT_ERROR_MSG, e)
        }
    }

    companion object {
        private const val DEFAULT_ERROR_MSG = "Failed to fetch SAML token from fss-gateway"
        const val TOKEN_EXCHANGE_PATH = "/rest/v1/sts/token/exchange?serviceUserId=3"

        private fun body(token: String) =
            BodyInserters
                .fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                .with("subject_token_type", "urn:ietf:params:oauth:token-type:access_token")
                .with("subject_token", token)
    }
}