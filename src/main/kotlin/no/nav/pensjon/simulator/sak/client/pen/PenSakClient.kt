package no.nav.pensjon.simulator.sak.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.sak.client.SakClient
import no.nav.pensjon.simulator.sak.client.pen.acl.PenSakSpec
import no.nav.pensjon.simulator.sak.client.pen.acl.PenVirkningsdatoResult
import no.nav.pensjon.simulator.sak.client.pen.acl.PenVirkningsdatoResultMapper
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
class PenSakClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), SakClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val cache: Cache<Pid, FoersteVirkningDatoCombo> = createCache("personVirkningDato", cacheManager)

    override fun fetchPersonVirkningDato(pid: Pid): FoersteVirkningDatoCombo =
        cache.getIfPresent(pid) ?: fetchFreshData(pid).also { cache.put(pid, it) }

    private fun fetchFreshData(pid: Pid): FoersteVirkningDatoCombo {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenSakSpec(pid.value))
                .retrieve()
                .bodyToMono(PenVirkningsdatoResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenVirkningsdatoResultMapper::fromDto)
                ?: emptyFoersteVirkningDatoCombo()
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
        private const val PATH = "v1/simulering/person-virkningsdato"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE

        private fun emptyFoersteVirkningDatoCombo() =
            FoersteVirkningDatoCombo(
                foersteVirkningDatoGrunnlagListe = emptyList()
            )
    }
}
