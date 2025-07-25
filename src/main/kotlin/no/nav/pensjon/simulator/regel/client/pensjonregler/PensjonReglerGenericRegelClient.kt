package no.nav.pensjon.simulator.regel.client.pensjonregler

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.regel.client.GenericRegelClient
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
    @Value("\${ps.regler.url}") baseUrl: String,
    @Value("\${ps.regler.gcp.url}") gcpBaseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    @Qualifier("regler") private val objectMapper: ObjectMapper,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), GenericRegelClient {
    val log = KotlinLogging.logger { }
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()
    private val webClient2 = webClientBuilder.baseUrl(gcpBaseUrl).build()

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

        if (serviceName == "hentGrunnbelopListe" || serviceName == "vilkarsprovAlderspensjon2025") {
            val start = System.currentTimeMillis()
            val body2 = webClient2
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

            val fssStart = System.currentTimeMillis()
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
            val stop = System.currentTimeMillis()

            log.info { "FSS: ${stop - fssStart} ms - GCP: ${fssStart - start} ms - $serviceName - response length ${body2?.length}" }
            return requireNonNull(objectMapper.readValue(requireNonNull(responseBody), responseClass) as T)
        }

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
        sakId?.let { headers["sakId"] = it }
        headers.contentType = MediaType.APPLICATION_JSON
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    override fun service() = service

    companion object {
        private const val BASE_PATH = "api"

        private val service = EgressService.PENSJON_REGLER
    }
}
