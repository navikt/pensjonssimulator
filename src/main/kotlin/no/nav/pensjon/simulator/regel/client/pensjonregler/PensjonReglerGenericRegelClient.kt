package no.nav.pensjon.simulator.regel.client.pensjonregler

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.regel.client.GenericRegelClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.Objects.requireNonNull

// PEN: PensjonReglerRestConsumerService
@Component
class PensjonReglerGenericRegelClient(
    @Value("\${ps.fss-gw.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    @Qualifier("regler") private val objectMapper: ObjectMapper,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), GenericRegelClient {
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    // regelServiceApi
    override fun <K, T : Any> makeRegelCall(
        request: T,
        responseClass: Class<*>,
        serviceName: String,
        map: Map<String, Any>?,
        sakId: String?
    ): K {
        return try {
            callPensjonRegler(serviceName, request, responseClass, map, sakId)
        } catch (e: Exception) {
            throw ImplementationUnrecoverableException("Request failed to pensjon-regler", e)
        }
    }

    // pensjonReglerRest
    private fun <T> callPensjonRegler(
        serviceName: String,
        request: Any,
        responseClass: Class<*>,
        extra: Map<String, Any>?,
        sakId: String?
    ): T {
        val uri = "$BASE_PATH/$serviceName"

        val responseBody = webClient
            .post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .headers(::setHeaders)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String::class.java)
            .retryWhen(retryBackoffSpec(uri))
            .block()

        return requireNonNull(objectMapper.readValue(requireNonNull(responseBody), responseClass) as T)
    }

    // makeHttpHeaders
    private fun setHeaders(headers: HttpHeaders, sakId: String? = null) {
        with(EgressAccess.token(service).value) { headers.setBearerAuth(this) }
        sakId?.let { headers["sakId"] = it }
        headers.contentType = MediaType.APPLICATION_JSON
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    override fun service() = service

    companion object {
        private const val BASE_PATH = "api"

        private val service = EgressService.FSS_GATEWAY
    }
}
