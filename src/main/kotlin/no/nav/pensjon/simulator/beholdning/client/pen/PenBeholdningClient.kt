package no.nav.pensjon.simulator.beholdning.client.pen

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.beholdning.*
import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import no.nav.pensjon.simulator.beholdning.client.pen.acl.PenFolketrygdBeholdningResult
import no.nav.pensjon.simulator.beholdning.client.pen.acl.PenFolketrygdBeholdningResultMapper
import no.nav.pensjon.simulator.beholdning.client.pen.acl.PenFolketrygdBeholdningSpecMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenBeholdningClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
) : ExternalServiceClient(retryAttempts), BeholdningClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun service() = service

    override fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning {
        val uri = "$BASE_PATH/$PATH"
        val dto = PenFolketrygdBeholdningSpecMapper.toDto(spec)
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
                .bodyToMono(PenFolketrygdBeholdningResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenFolketrygdBeholdningResultMapper::fromDto)
                ?: nullResult()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val BASE_PATH = "pen/springapi"
        private const val PATH = "simulering/v1/simuler-folketrygdbeholdning"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE

        private fun nullResult() = FolketrygdBeholdning(emptyList())
    }
}
