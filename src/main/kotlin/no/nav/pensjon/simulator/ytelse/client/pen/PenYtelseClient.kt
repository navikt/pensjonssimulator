package no.nav.pensjon.simulator.ytelse.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec
import no.nav.pensjon.simulator.ytelse.client.YtelseClient
import no.nav.pensjon.simulator.ytelse.client.pen.acl.PenLoependeYtelserResultMapper
import no.nav.pensjon.simulator.ytelse.client.pen.acl.PenLoependeYtelserResultV1
import no.nav.pensjon.simulator.ytelse.client.pen.acl.PenLoependeYtelserSpecMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenYtelseClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), YtelseClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()
    private val cache: Cache<LoependeYtelserSpec, LoependeYtelserResult> = createCache("loependeYtelser", cacheManager)

    //TODO Handle PEN226BrukerHarLopendeAPPaGammeltRegelverkException, PEN223BrukerHarIkkeLopendeAlderspensjonException
    // in PEN EndringApLoependeYtelserService and propagate error info to response received here
    override fun fetchLoependeYtelser(spec: LoependeYtelserSpec): LoependeYtelserResult =
        cache.getIfPresent(spec) ?: fetchFreshLoependeYtelser(spec).also { cache.put(spec, it) }

    private fun fetchFreshLoependeYtelser(spec: LoependeYtelserSpec): LoependeYtelserResult {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenLoependeYtelserSpecMapper.toDto(spec))
                .retrieve()
                .bodyToMono(PenLoependeYtelserResultV1::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenLoependeYtelserResultMapper::fromDto)
                ?: LoependeYtelserResult(alderspensjon = null, afpPrivat = null)
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
        private const val PATH = "ytelser/v1/loepende"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
