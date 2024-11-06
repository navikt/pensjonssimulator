package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.LivsvarigOffentligAfpClient
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl.TpSimLivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl.TpSimLivsvarigOffentligAfpResultMapper.fromDto
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim.acl.TpSimLivsvarigOffentligAfpSpecMapper.toDto
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
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

/**
 * Implementasjon av LivsvarigOffentligAfpClient som bruker Tjenestepensjon-simulering
 * (github.com/navikt/tjenestepensjon-simulering)
 * TP = tjenestepensjon
 */
@Component
class TpSimuleringLivsvarigOffentligAfpClient(
    @Value("\${ps.tp-simulering.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), LivsvarigOffentligAfpClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun simuler(spec: LivsvarigOffentligAfpSpec): LivsvarigOffentligAfpResult {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(toDto(spec))
                .retrieve()
                .bodyToMono(TpSimLivsvarigOffentligAfpResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(::fromDto)!!
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        with(EgressAccess.token(service).value) {
            headers.setBearerAuth(this)
            log.debug { "Token: $this" }
        }

        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val BASE_PATH = "simulering"
        private const val PATH = "afp-offentlig-livsvarig"
        private val service = EgressService.TJENESTEPENSJON_SIMULERING
    }
}
