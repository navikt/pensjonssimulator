package no.nav.pensjon.simulator.opptjening.client.pen

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningSpec
import no.nav.pensjon.simulator.opptjening.client.pen.acl.PenOpptjening
import no.nav.pensjon.simulator.opptjening.client.OpptjeningMedBeholdningClient
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.opptjening.Pensjonsopptjening
import no.nav.pensjon.simulator.opptjening.client.pen.acl.PenOpptjeningSpec
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
class PenOpptjeningClient(
    @Value($$"${ps.pen.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid,
) : ExternalServiceClient(retryAttempts), OpptjeningMedBeholdningClient {

    private val webClient = webClientBase.withBaseUrl(baseUrl)

    private val cache: Cache<OpptjeningMedBeholdningSpec, Pensjonsopptjening> =
        createCache("pen-pensjonsopptjening", cacheManager)

    override fun fetchOpptjening(spec: OpptjeningMedBeholdningSpec): Pensjonsopptjening =
        cache.getIfPresent(spec) ?: fetchFreshOpptjening(spec).also { cache.put(spec, it) }

    override fun service() = service

    private fun fetchFreshOpptjening(spec: OpptjeningMedBeholdningSpec): Pensjonsopptjening {
        val uri = "$BASE_PATH/$BEHOLDNINGER_MED_GRUNNLAG_PATH"
        val dto = PenOpptjeningSpec.fromInternalValue(spec)

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono<PenOpptjening>()
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.toInternalValue()
                ?: Pensjonsopptjening.emptyInstance()
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
        private const val BEHOLDNINGER_MED_GRUNNLAG_PATH = "beholdning/v1/beholdninger-med-grunnlag"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}