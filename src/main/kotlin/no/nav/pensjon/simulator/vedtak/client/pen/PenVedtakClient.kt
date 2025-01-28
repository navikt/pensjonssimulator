package no.nav.pensjon.simulator.vedtak.client.pen

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import no.nav.pensjon.simulator.vedtak.client.VedtakClient
import no.nav.pensjon.simulator.vedtak.client.pen.acl.PenVedtakResultV1
import no.nav.pensjon.simulator.vedtak.client.pen.acl.PenVedtakStatusSpec
import no.nav.pensjon.simulator.vedtak.client.pen.acl.PenVedtakSpecV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate

@Component
class PenVedtakClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), VedtakClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun tidligsteKapittel20VedtakGjelderFom(pid: Pid, sakType: SakTypeEnum): LocalDate? {
        val uri = "$BASE_PATH/$DATO_RESOURCE"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenVedtakSpecV1(pid.value, sakType))
                .retrieve()
                .bodyToMono(PenVedtakResultV1::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.dato
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun fetchVedtakStatus(pid: Pid, fom: LocalDate?): VedtakStatus {
        val uri = "$BASE_PATH/$STATUS_RESOURCE"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenVedtakStatusSpec(pid.value, fom))
                .retrieve()
                .bodyToMono(VedtakStatus::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?: VedtakStatus(harGjeldendeVedtak = false, harGjenlevenderettighet = false)
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
        private const val BASE_PATH = "api/vedtak"
        private const val DATO_RESOURCE = "v1/tidligste-kap20-fom"
        private const val STATUS_RESOURCE = "v1/status-for-simulator"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
