package no.nav.pensjon.simulator.beholdning.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import no.nav.pensjon.simulator.beholdning.client.pen.acl.PenBeholdningerMedGrunnlagSpecMapper
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
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
class PenBeholdningClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid,
) : ExternalServiceClient(retryAttempts), BeholdningClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    private val cache: Cache<BeholdningerMedGrunnlagSpec, BeholdningerMedGrunnlagResult> =
        createCache("beholdningerMedGrunnlag", cacheManager)

    override fun fetchBeholdningerMedGrunnlag(spec: BeholdningerMedGrunnlagSpec): BeholdningerMedGrunnlagResult =
        cache.getIfPresent(spec) ?: fetchFreshBeholdninger(spec).also { cache.put(spec, it) }

    override fun service() = service

    private fun fetchFreshBeholdninger(spec: BeholdningerMedGrunnlagSpec): BeholdningerMedGrunnlagResult {
        val uri = "$BASE_PATH/$BEHOLDNINGER_MED_GRUNNLAG_PATH"
        val dto = PenBeholdningerMedGrunnlagSpecMapper.toDto(spec)
        log.debug { "POST to URI: '$uri' with body '$dto'" }

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(BeholdningerMedGrunnlagResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?: emptyBeholdningerMedGrunnlagResult()
            // NB: No mapping of response; it is assumed that PEN returns regler-compatible response body
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
        private const val BASE_PATH = "pen/api"
        private const val BEHOLDNINGER_MED_GRUNNLAG_PATH = "beholdning/v1/beholdninger-med-grunnlag"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE

        private fun emptyBeholdningerMedGrunnlagResult() =
            BeholdningerMedGrunnlagResult(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = emptyList(),
                foerstegangstjeneste = null
            )
    }
}
