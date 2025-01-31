package no.nav.pensjon.simulator.krav.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.krav.client.KravClient
import no.nav.pensjon.simulator.krav.client.pen.acl.PenKravSpec
import no.nav.pensjon.simulator.krav.client.pen.acl.PenKravhode
import no.nav.pensjon.simulator.krav.client.pen.acl.PenKravhodeMapper
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenKravClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), KravClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()
    private val cache: Cache<Long, Kravhode> = createCache("kravhode", cacheManager)

    override fun fetchKravhode(kravhodeId: Long): Kravhode =
        cache.getIfPresent(kravhodeId) ?: fetchFreshData(kravhodeId).also { cache.put(kravhodeId, it) }

    private fun fetchFreshData(kravhodeId: Long): Kravhode {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenKravSpec(kravhodeId))
                .retrieve()
                .bodyToMono(PenKravhode::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenKravhodeMapper::kravhode)
                ?: Kravhode()
            // NB: No mapping of response; it is assumed that PEN returns regler-compatible Kravhode
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
        private const val BASE_PATH = "api"
        private const val PATH = "v1/simulering/kravhode"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
