package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.fromDto
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.toDto
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.toEntity

@Component
class PensjonOpptjeningSimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient(
    @Value($$"${ps.pensjon-opptjening-afp-api.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    private val traceAid: TraceAid,
    webClientBase: WebClientBase
) : ExternalServiceClient(retryAttempts), SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient {

    private val log = KotlinLogging.logger {}
    private val afpBeholdningWebClient: WebClient = webClientBase.withBaseUrl(baseUrl)

    override fun simuler(
        spec: LivsvarigOffentligAfpSpec
    ): List<SimulerLivsvarigOffentligAfpBeholdningsperiode> {
        val uri = "/api/simuler"

        return try {
            afpBeholdningWebClient.post()
                .uri(uri)
                .headers(::setHeaders)
                .bodyValue(toDto(spec))
                .retrieve()
                .toEntity<SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult>()
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.body?.let(::fromDto)
                ?: emptyList()
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
        with(EgressAccess.token(service).value) {
            headers.setBearerAuth(this)
            log.debug { "Token: $this" }
        }
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun toString(e: EgressException, uri: String): String = "Failed calling $uri"

    override fun service(): EgressService = service

    companion object {
        val service = EgressService.AFP_BEHOLDNING_API
    }
}
