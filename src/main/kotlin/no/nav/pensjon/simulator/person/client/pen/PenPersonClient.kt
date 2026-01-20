package no.nav.pensjon.simulator.person.client.pen

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.person.client.PersonClient
import no.nav.pensjon.simulator.person.client.pen.acl.PenPersonHistorikkMapper
import no.nav.pensjon.simulator.person.client.pen.acl.PenPersonResult
import no.nav.pensjon.simulator.person.client.pen.acl.PenPersonSpec
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
class PenPersonClient(
    @Value($$"${ps.pen.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), PersonClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val cache: Cache<Pid, PenPerson> = createCache("person", cacheManager)

    override fun fetchPersonerVedPid(pidListe: List<Pid>): Map<Pid, PenPerson> {
        val cacheState = analyseCache(pidListe)
        val hits: List<PenPerson> = cacheState.hits
        val misses: List<Pid> = cacheState.misses

        if (misses.isEmpty())
            return hits.associateBy { it.pid!! }

        val personerVedPid: MutableMap<Pid, PenPerson> =
            fetchFreshPersoner(misses).apply {
                forEach { cache.put(it.key, it.value) }
            }.toMutableMap()

        hits.forEach { personerVedPid[it.pid!!] = it }
        return personerVedPid
    }

    private fun analyseCache(pidListe: List<Pid>): CacheState {
        val hits: MutableList<PenPerson> = mutableListOf()
        val misses: MutableList<Pid> = mutableListOf()

        pidListe.forEach {
            val person: PenPerson? = cache.getIfPresent(it)
            if (person == null) misses.add(it) else hits.add(person)
        }

        return CacheState(hits, misses)
    }

    private fun fetchFreshPersoner(pidListe: List<Pid>): Map<Pid, PenPerson> {
        val uri = "$BASE_PATH/$PATH"

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(PenPersonSpec(pidListe.map { it.value }))
                .retrieve()
                .bodyToMono(PenPersonResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let { PenPersonHistorikkMapper.fromDto(it.personerVedPid) }.orEmpty()
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

    private data class CacheState(
        val hits: List<PenPerson>,
        val misses: List<Pid>
    )

    private companion object {
        private const val BASE_PATH = "api/persondata"
        private const val PATH = "v1/historikk"
        private val service = EgressService.PENSJONSFAGLIG_KJERNE
    }
}
