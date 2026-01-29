package no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.person.relasjon.PersonRelasjonStatus
import no.nav.pensjon.simulator.person.relasjon.PersonPar
import no.nav.pensjon.simulator.person.relasjon.client.PersonRelasjonClient
import no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl.SamboerStatusResultDto
import no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl.SamboerStatusResultMapper.fromDto
import no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl.PersonParSpecDto
import no.nav.pensjon.simulator.person.relasjon.client.pensjonpersondata.acl.PersonParSpecMapper.dto
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
class PensjonPersondataClient(
    @Value($$"${ps.pensjonpersondata.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), PersonRelasjonClient {

    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val cache: Cache<PersonPar, PersonRelasjonStatus> = createCache("personrelasjon", cacheManager)

    override fun fetchPersonRelasjonStatus(personer: PersonPar): PersonRelasjonStatus? =
        cache.getIfPresent(personer) ?: fetchFreshStatus(personer)?.also { cache.put(personer, it) }

    private fun fetchFreshStatus(relasjon: PersonPar): PersonRelasjonStatus? {
        val uri = "$PATH/?dato=${relasjon.dato}"

        return try {
            webClient
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers { setHeaders(headers = it, spec = dto(source = relasjon)) }
                .retrieve()
                .bodyToMono(SamboerStatusResultDto::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(::fromDto)
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders, spec: PersonParSpecDto) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.PERSON_ID] = spec.pid
        headers[CustomHttpHeaders.ANNEN_PERSON_ID] = spec.annenPid
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    private companion object {
        private const val PATH = "/api/adresse/bostedsadresse/borSammen"
        private val service = EgressService.PENSJON_PERSONDATA
    }
}
