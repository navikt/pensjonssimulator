package no.nav.pensjon.simulator.ufoere.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.ufoere.client.UfoeretrygdUtbetalingClient
import no.nav.pensjon.simulator.ufoere.client.pen.acl.PenUfoeretrygdUtbetalingResult
import no.nav.pensjon.simulator.ufoere.client.pen.acl.PenUfoeretrygdUtbetalingSpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenUfoeretrygdUtbetalingClient(
    @Value($$"${ps.pen.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), UfoeretrygdUtbetalingClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val cache: Cache<Long, List<UtbetalingsgradUT>> = createCache("utbetalingsgrader", cacheManager)

    override fun fetchUtbetalingsgradListe(penPersonId: Long): List<UtbetalingsgradUT> =
        cache.getIfPresent(penPersonId) ?: fetchFreshData(penPersonId).also { cache.put(penPersonId, it) }

    private fun fetchFreshData(penPersonId: Long): List<UtbetalingsgradUT> {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenUfoeretrygdUtbetalingSpec(penPersonId))
                .retrieve()
                .bodyToMono(PenUfoeretrygdUtbetalingResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.utbetalingsgradListe.orEmpty()
            // NB: No mapping of response; it is assumed that PEN returns regler-compatible response body
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
        private const val BASE_PATH = "api/uforetrygd"
        private const val PATH = "v1/utbetalingsgrader"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
