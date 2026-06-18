package no.nav.pensjon.simulator.krav.client.pen

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.krav.client.KravClient
import no.nav.pensjon.simulator.krav.client.pen.acl.PenKravSpec
import no.nav.pensjon.simulator.krav.client.pen.acl.PenKravhode
import no.nav.pensjon.simulator.krav.client.pen.acl.PenKravhodeMapper
import no.nav.pensjon.simulator.krav.client.pen.acl.PenUttaksgradSpec
import no.nav.pensjon.simulator.person.Pid
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
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class PenKravClient(
    @Value($$"${ps.pen.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), KravClient {

    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val kravhodeCache: Cache<Long, Kravhode> = createCache("kravhode", cacheManager)
    private val uttaksgradCache: Cache<Pid, List<Uttaksgrad>> = createCache("uttaksgrader", cacheManager)

    override fun fetchKravhode(kravhodeId: Long): Kravhode =
        kravhodeCache.getIfPresent(kravhodeId) ?: fetchFreshKravhode(kravhodeId).also { kravhodeCache.put(kravhodeId, it) }

    override fun fetchUttaksgrader(pid: Pid): List<Uttaksgrad> =
        uttaksgradCache.getIfPresent(pid) ?: fetchFreshUttaksgrader(pid).also { uttaksgradCache.put(pid, it) }

    private fun fetchFreshKravhode(kravhodeId: Long): Kravhode {
        val uri = "$BASE_PATH/$KRAVHODE_PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenKravSpec(kravhodeId))
                .retrieve()
                .bodyToMono<PenKravhode>()
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenKravhodeMapper::kravhode)
                ?: Kravhode()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun fetchFreshUttaksgrader(pid: Pid): List<Uttaksgrad> {
        val uri = "$BASE_PATH/$UTTAKSGRAD_PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenUttaksgradSpec(pid.value))
                .retrieve()
                .bodyToMono<List<Uttaksgrad>>()
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?: emptyList()
            // NB: No mapping of response; it is assumed that PEN returns regler-compatible Uttaksgrad
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val BASE_PATH = "api/v1/simulering"
        private const val KRAVHODE_PATH = "kravhode"
        private const val UTTAKSGRAD_PATH = "uttaksgrader"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}