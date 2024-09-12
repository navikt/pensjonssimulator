package no.nav.pensjon.simulator.tech.sporing.client.samhandling

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.Sporing
import no.nav.pensjon.simulator.tech.sporing.client.SporingsloggClient
import no.nav.pensjon.simulator.tech.sporing.client.samhandling.acl.SamhandlingSporingMapper
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
class SamhandlingSporingsloggClient(
    @Value("\${ps.sporingslogg.url}") baseUrl: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
) : ExternalServiceClient("0"), SporingsloggClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun service() = service

    override fun log(sporing: Sporing) {
        val uri = PATH
        val dto = SamhandlingSporingMapper.toDto(sporing)
        log.debug { "POST to URI: '$uri' with body '$dto'" }

        try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(retryBackoffSpec(uri))
                .block()
            log.info { "Done" }
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
        private const val PATH = "sporingslogg"

        private val service = EgressService.SPORINGSLOGG
    }
}
