package no.nav.pensjon.simulator.common.client.pen

import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.Pingable
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenPingClient(
    @param:Value($$"${ps.pen.url}") private val baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    private val traceAid: TraceAid,
) : ExternalServiceClient(retryAttempts), Pingable {

    private val webClient = webClientBase.withBaseUrl(baseUrl)

    override fun ping(): PingResult {
        val relativeUri = PING_PATH
        val absoluteUri = "$baseUrl/$relativeUri"

        return try {
            val responseBody =
                webClient
                    .get()
                    .uri(relativeUri)
                    .headers(::setHeaders)
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .retryWhen(retryBackoffSpec(relativeUri))
                    .block()
                    ?: ""

            up(endpoint = absoluteUri, message = responseBody)
        } catch (e: EgressException) {
            // Happens if failing to get an access token
            down(absoluteUri, message = e.message)
        } catch (e: WebClientRequestException) {
            down(absoluteUri, message = e.message)
        } catch (e: WebClientResponseException) {
            down(absoluteUri, message = e.responseBodyAsString)
        }
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun up(endpoint: String, message: String) =
        PingResult(
            service = service(),
            status = ServiceStatus.UP,
            endpoint,
            message
        )

    private fun down(uri: String, message: String?) =
        PingResult(
            service,
            status = ServiceStatus.DOWN,
            endpoint = uri,
            message ?: "Failed calling $service"
        )

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    private companion object {
        private const val PING_PATH = "api/ping"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
