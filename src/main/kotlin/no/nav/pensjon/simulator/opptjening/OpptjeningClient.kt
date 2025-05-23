package no.nav.pensjon.simulator.opptjening

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.opptjening.dto.OpptjeningsgrunnlagExtractor
import no.nav.pensjon.simulator.opptjening.dto.OpptjeningsgrunnlagResponseDto
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.MetricResult
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate

@Component
class OpptjeningClient(
    @Value("\${ps.popp.url}") private val baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
    private val time: Time
) : ExternalServiceClient(retryAttempts), SisteLignetInntekt {

    private val webClient = webClientBuilder.baseUrl(baseUrl).build()
    private val log = KotlinLogging.logger {}

    override fun hentSisteLignetInntekt(pid: Pid): Inntekt {
        val url = "$baseUrl/$OPPTJENINGSGRUNNLAG_PATH"
        log.debug { "GET from URL: '$url'" }

        return try {
            webClient
                .get()
                .uri("/$OPPTJENINGSGRUNNLAG_PATH")
                .headers { setHeaders(it, pid) }
                .retrieve()
                .bodyToMono(OpptjeningsgrunnlagResponseDto::class.java)
                .retryWhen(retryBackoffSpec(url))
                .block()
                ?.let(OpptjeningsgrunnlagExtractor::fromDto)
                .also { countCalls(MetricResult.OK) }
                ?: zeroInntekt()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun setHeaders(headers: HttpHeaders, pid: Pid) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
        headers[CustomHttpHeaders.PID] = pid.value
    }

    private fun zeroInntekt() =
        Inntekt(
            aarligBeloep = 0,
            fom = LocalDate.of(time.today().year, 1, 1)
        )

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"
    override fun service() = service

    companion object {
        private const val OPPTJENINGSGRUNNLAG_PATH = "popp/api/opptjeningsgrunnlag"
        private const val PING_PATH = "$OPPTJENINGSGRUNNLAG_PATH/ping"
        private val service = EgressService.PENSJONSOPPTJENING
    }
}
