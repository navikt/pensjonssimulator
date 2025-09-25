package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.beholdning

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.AFPGrunnlagBeholdningPeriode
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.SimulerAFPBeholdningGrunnlagRequest
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.SimulerAFPBeholdningGrunnlagResponse
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.AFPBeholdningClient
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import java.time.Duration

@Component
class WebClientAFPBeholdningClient(
    @Value("\${ps.pensjon-opptjening-afp-api.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    private val traceAid: TraceAid,
    webClientBuilder: WebClient.Builder
) : ExternalServiceClient(retryAttempts), AFPBeholdningClient {

    private val log = KotlinLogging.logger {}
    private val afpBeholdningWebClient: WebClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun simulerAFPBeholdningGrunnlag(
        request: SimulerAFPBeholdningGrunnlagRequest
    ): List<AFPGrunnlagBeholdningPeriode> {
        return try {
            afpBeholdningWebClient.post()
                .uri("/api/simuler")
                .headers { setHeaders(it) }
                .bodyValue(request)
                .retrieve()
                .toEntity(SimulerAFPBeholdningGrunnlagResponse::class.java)
                .retryWhen(
                    Retry.backoff(4, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .jitter(0.3) // 30% tilfeldig forsinkelse
                        .doBeforeRetry { retrySignal ->
                            log.warn {
                                "Retrying henting av AFP Beholdninger due to: ${retrySignal.failure().message}, attempt: ${retrySignal.totalRetries() + 1}"
                            }
                        }
                        .onRetryExhaustedThrow { _, _ -> RuntimeException("Failed to get AFP Beholdninger after all retries") }
                )
                .block()
                ?.body!!
                .pensjonsBeholdningsPeriodeListe
        } catch (e: WebClientRequestException) {
            val errorMessage = "Request to get AFP Beholdninger failed: ${e.message}"
            log.error(e) { errorMessage }
            throw RuntimeException(errorMessage)
        } catch (e: WebClientResponseException) {
            val errorMessage = "Request to get AFP Beholdninger failed with response: ${e.responseBodyAsString}"
            log.error(e) { errorMessage }
            throw RuntimeException(errorMessage)
        }
    }

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun toString(e: EgressException, uri: String): String = "Failed calling $uri"

    override fun service(): EgressService = service

    companion object {
        val service = EgressService.AFP_BEHOLDNING_API
    }
}
