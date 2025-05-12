package no.nav.pensjon.simulator.normalder.client.pen

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.normalder.NormertPensjonsalder
import no.nav.pensjon.simulator.normalder.client.NormertPensjonsalderClient
import no.nav.pensjon.simulator.normalder.client.pen.acl.PenNormAlderResultMapper
import no.nav.pensjon.simulator.normalder.client.pen.acl.PenNormAlderResult
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
class PenNormertPensjonsalderClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid,
) : ExternalServiceClient(retryAttempts), NormertPensjonsalderClient {

    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    private val cache: Cache<Int, List<NormertPensjonsalder>> =
        createCache("normertPensjonsalder", cacheManager)

    override fun fetchNormAlderListe(): List<NormertPensjonsalder> =
        cache.getIfPresent(1) ?: fetchFreshData().also { cache.put(1, it) }

    override fun service() = service

    private fun fetchFreshData(): List<NormertPensjonsalder> {
        val uri = "$BASE_PATH/$RESOURCE"

        return try {
            webClient
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono(PenNormAlderResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenNormAlderResultMapper::fromDto)
                ?: emptyList()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    private companion object {
        private const val BASE_PATH = "api"
        private const val RESOURCE = "normertpensjonsalder"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
