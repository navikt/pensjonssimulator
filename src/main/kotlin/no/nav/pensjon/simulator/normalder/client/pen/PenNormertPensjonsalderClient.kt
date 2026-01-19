package no.nav.pensjon.simulator.normalder.client.pen

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.normalder.Aldersgrenser
import no.nav.pensjon.simulator.normalder.client.NormertPensjonsalderClient
import no.nav.pensjon.simulator.normalder.client.pen.acl.PenNormalderResult
import no.nav.pensjon.simulator.normalder.client.pen.acl.PenNormalderResultMapper
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenNormertPensjonsalderClient(
    @Value($$"${ps.pen.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid,
) : ExternalServiceClient(retryAttempts), NormertPensjonsalderClient {

    private val webClient = webClientBase.withBaseUrl(baseUrl)

    private val cache: Cache<Int, List<Aldersgrenser>> =
        createCache("normertPensjonsalder", cacheManager)

    override fun fetchNormalderListe(): List<Aldersgrenser> =
        cache.getIfPresent(1) ?: fetchFreshData().also { cache.put(1, it) }

    override fun service() = service

    private fun fetchFreshData(): List<Aldersgrenser> {
        val uri = "$BASE_PATH/$RESOURCE"

        return try {
            webClient
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono(PenNormalderResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenNormalderResultMapper::fromDto)
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
