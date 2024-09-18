package no.nav.pensjon.simulator.alderspensjon.client.pen

import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.client.AlderspensjonClient
import no.nav.pensjon.simulator.alderspensjon.client.pen.acl.*
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenAlderspensjonClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), AlderspensjonClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun service() = service

    override fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult {
        val uri = "$BASE_PATH/$PATH"
        val dto = PenAlderspensjonSpecMapper.toDto(spec)
        log.debug { "POST to URI: '$uri' with body '$dto'" }

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PenAlderspensjonResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenAlderspensjonResultMapper::fromDto)
                ?: nullResult()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        with(EgressAccess.token(service).value) {
            headers.setBearerAuth(this)
            log.debug { "Token: $this" }
        }

        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val BASE_PATH = "pen/api"
        private const val PATH = "ekstern/simulering/v4/alderspensjon"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE

        private fun nullResult() = AlderspensjonResult(
            simuleringSuksess = false,
            aarsakListeIkkeSuksess = emptyList(),
            alderspensjon = emptyList(),
            forslagVedForLavOpptjening = null,
            harUttak = false
        )
    }
}
