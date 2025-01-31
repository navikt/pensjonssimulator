package no.nav.pensjon.simulator.vedtak.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.vedtak.client.VedtakClient
import no.nav.pensjon.simulator.vedtak.client.pen.acl.PenVedtakResultV1
import no.nav.pensjon.simulator.vedtak.client.pen.acl.PenVedtakSpecV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCacheManager
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
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), VedtakClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    private val cache: Cache<PenVedtakSpecV1, PenVedtakResultV1> =
        createCache("tidligsteKapittel20VedtakGjelderFom", cacheManager)

    override fun tidligsteKapittel20VedtakGjelderFom(pid: Pid, sakType: SakTypeEnum): LocalDate? {
        val spec = PenVedtakSpecV1(pid.value, sakType)
        val result = cache.getIfPresent(spec) ?: fetchFreshData(spec).also { cache.put(spec, it) }
        return result.dato
    }

    private fun fetchFreshData(spec: PenVedtakSpecV1): PenVedtakResultV1 {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(spec)
                .retrieve()
                .bodyToMono(PenVedtakResultV1::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?: PenVedtakResultV1(dato = null)
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
        private const val PATH = "v1/tidligste-kap20-fom"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
