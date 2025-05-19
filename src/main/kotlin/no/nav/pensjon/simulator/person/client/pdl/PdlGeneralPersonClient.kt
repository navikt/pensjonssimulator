package no.nav.pensjon.simulator.person.client.pdl

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.person.client.GeneralPersonClient
import no.nav.pensjon.simulator.person.client.pdl.acl.PdlPersonMapper
import no.nav.pensjon.simulator.person.client.pdl.acl.PdlPersonResult
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
import java.time.LocalDate

@Component
class PdlGeneralPersonClient(
    @Value("\${ps.persondata.url}") private val baseUrl: String,
    webClientBuilder: WebClient.Builder,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String
) : ExternalServiceClient(retryAttempts), GeneralPersonClient {

    private val webClient: WebClient = webClientBuilder.baseUrl(baseUrl).build()
    private val cache: Cache<Pid, LocalDate> = createCache("foedselsdato", cacheManager)
    private val log = KotlinLogging.logger {}

    override fun service() = service

    override fun fetchFoedselsdato(pid: Pid): LocalDate? =
        cache.getIfPresent(pid) ?: fetchFreshData(pid)?.also { cache.put(pid, it) }

    private fun fetchFreshData(pid: Pid): LocalDate? {
        val uri = "/$RESOURCE"

        return try {
            webClient
                .post()
                .uri(uri)
                .headers(::setHeaders)
                .bodyValue(foedselsdatoQuery(pid))
                .retrieve()
                .bodyToMono(PdlPersonResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.also { warnings(it)?.let { log.warn { it } } }
                ?.let(PdlPersonMapper::fromDto)
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $baseUrl$uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $baseUrl$uri"

    private fun setHeaders(headers: HttpHeaders) {
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.BEHANDLINGSNUMMER] = BEHANDLINGSNUMMER
        headers[CustomHttpHeaders.THEME] = THEME
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val RESOURCE = "graphql"
        private const val THEME = "PEN"

        // https://behandlingskatalog.nais.adeo.no/process/team/d55cc783-7850-4606-9ff6-1fc44b646c9d/91a4e540-5e39-4c10-971f-49b48f35fe11
        private const val BEHANDLINGSNUMMER = "B353"
        private val service = EgressService.PERSONDATA

        private fun foedselsdatoQuery(pid: Pid) = """{
	"query": "query(${"$"}ident: ID!) { hentPerson(ident: ${"$"}ident) { foedselsdato { foedselsdato } } }",
	"variables": {
		"ident": "${pid.value}"
	}
}"""

        private fun warnings(response: PdlPersonResult): String? =
            response.extensions?.warnings?.joinToString {
                (it.message ?: "-") + " (${warningDetails(it.details)})"
            }

        private fun warningDetails(details: Any?): String =
            when (details) {
                is String -> details
                is LinkedHashMap<*, *> -> (details["missing"] as ArrayList<*>).joinToString()
                else -> details.toString()
            }
    }
}
