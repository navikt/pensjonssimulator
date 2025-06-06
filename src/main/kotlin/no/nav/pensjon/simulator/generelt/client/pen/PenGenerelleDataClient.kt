package no.nav.pensjon.simulator.generelt.client.pen

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.generelt.GenerelleData
import no.nav.pensjon.simulator.generelt.GenerelleDataSpec
import no.nav.pensjon.simulator.generelt.Person
import no.nav.pensjon.simulator.generelt.client.GenerelleDataClient
import no.nav.pensjon.simulator.generelt.client.pen.acl.PenGenerelleDataResult
import no.nav.pensjon.simulator.generelt.client.pen.acl.PenGenerelleDataResultMapper
import no.nav.pensjon.simulator.generelt.client.pen.acl.PenGenerelleDataSpecMapper
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
class PenGenerelleDataClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), GenerelleDataClient {

    private val webClient = webClientBuilder.baseUrl(baseUrl).build()
    private val cache: Cache<GenerelleDataSpec, GenerelleData> = createCache("generelleData", cacheManager)

    override fun service() = service

    override fun fetchGenerelleData(spec: GenerelleDataSpec): GenerelleData =
        cache.getIfPresent(spec) ?: fetchFreshData(spec).also { cache.put(spec, it) }

    private fun fetchFreshData(spec: GenerelleDataSpec): GenerelleData {
        val uri = "$BASE_PATH/$PATH"
        val dto = PenGenerelleDataSpecMapper.toDto(spec)

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PenGenerelleDataResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let(PenGenerelleDataResultMapper::fromDto)
                ?: nullResult()
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

    companion object {
        private const val BASE_PATH = "api"
        private const val PATH = "v1/simulering/generelle-data"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE

        private fun nullResult() =
            GenerelleData(
                person = Person(LocalDate.MIN, LandkodeEnum.NOR),
                privatAfpSatser = PrivatAfpSatser(),
                satsResultatListe = emptyList(),
            )
    }
}
