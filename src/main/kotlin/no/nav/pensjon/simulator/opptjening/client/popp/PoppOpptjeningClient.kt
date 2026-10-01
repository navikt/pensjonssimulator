package no.nav.pensjon.simulator.opptjening.client.popp

import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.opptjening.BeholdningSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSamling
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSpec
import no.nav.pensjon.simulator.opptjening.PensjonspoengSpec
import no.nav.pensjon.simulator.opptjening.client.OpptjeningClient
import no.nav.pensjon.simulator.opptjening.client.popp.acl.*
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import no.nav.pensjon.simulator.tech.metric.MetricResult
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import no.nav.pensjon.simulator.opptjening.client.popp.acl.inntekt.PoppOpptjeningsgrunnlagResult as PoppInntektResult

@Component
class PoppOpptjeningClient(
    @param:Value($$"${ps.popp.url}") private val baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    cacheManager: CaffeineCacheManager,
    private val traceAid: TraceAid,
    private val time: Time
) : ExternalServiceClient(retryAttempts), OpptjeningClient {

    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val log = KotlinLogging.logger {}

    private val inntektCache: Cache<Pid, LoependeInntekt> =
        createCache("popp-sist-lignede-inntekt", cacheManager)

    private val beholdningCache: Cache<BeholdningSpec, List<Pensjonsbeholdning>> =
        createCache("popp-beholdninger", cacheManager)

    private val grunnlagCache: Cache<OpptjeningsgrunnlagSpec, OpptjeningsgrunnlagSamling> =
        createCache("popp-opptjeningsgrunnlag", cacheManager)

    private val pensjonspoengCache: Cache<PensjonspoengSpec, List<Opptjeningsgrunnlag>> =
        createCache("popp-pensjonspoeng", cacheManager)

    override fun fetchSistLignedeInntekt(pid: Pid): LoependeInntekt =
        inntektCache.getIfPresent(pid) ?: fetchFreshSistLignedeInntekt(pid).also { inntektCache.put(pid, it) }

    override fun fetchBeholdninger(spec: BeholdningSpec): List<Pensjonsbeholdning> =
        beholdningCache.getIfPresent(spec) ?: fetchFreshBeholdninger(spec).also { beholdningCache.put(spec, it) }

    override fun fetchOpptjeningsgrunnlag(spec: OpptjeningsgrunnlagSpec): OpptjeningsgrunnlagSamling =
        grunnlagCache.getIfPresent(spec) ?: fetchFreshOpptjeningsgrunnlag(spec).also { grunnlagCache.put(spec, it) }

    override fun fetchPensjonspoeng(spec: PensjonspoengSpec): List<Opptjeningsgrunnlag> =
        pensjonspoengCache.getIfPresent(spec) ?: fetchFreshPensjonspoeng(spec).also { pensjonspoengCache.put(spec, it) }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun fetchFreshSistLignedeInntekt(pid: Pid): LoependeInntekt {
        val url = "$baseUrl/$OPPTJENINGSGRUNNLAG_GET_PATH"
        log.debug { "GET from URL: '$url'" }

        return try {
            webClient
                .get()
                .uri("/$OPPTJENINGSGRUNNLAG_GET_PATH")
                .headers { setHeadersForGet(it, pid) }
                .retrieve()
                .bodyToMono<PoppInntektResult>()
                .retryWhen(retryBackoffSpec(url))
                .block()
                ?.toInternalValue()
                .also { countCalls(MetricResult.OK) }
                ?: zeroInntekt()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun fetchFreshBeholdninger(spec: BeholdningSpec): List<Pensjonsbeholdning> {
        val url = "$baseUrl/$BEHOLDNING_PATH"
        log.debug { "POST to URL: '$url'" }
        val body = PoppBeholdningSpec.fromInternalValue(spec)

        return try {
            webClient
                .post()
                .uri("/$BEHOLDNING_PATH")
                .headers(::setHeaders)
                .bodyValue(body)
                .retrieve()
                .bodyToMono<PoppBeholdningResult>()
                .retryWhen(retryBackoffSpec(url))
                .block()
                ?.toInternalValue()
                .also { countCalls(MetricResult.OK) }
                ?: emptyList()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun fetchFreshOpptjeningsgrunnlag(spec: OpptjeningsgrunnlagSpec): OpptjeningsgrunnlagSamling {
        val url = "$baseUrl/$OPPTJENINGSGRUNNLAG_PATH"
        log.debug { "POST to URL: '$url'" }
        val body = PoppOpptjeningsgrunnlagSpec.fromInternalValue(spec)

        return try {
            webClient
                .post()
                .uri("/$OPPTJENINGSGRUNNLAG_PATH")
                .headers(::setHeaders)
                .bodyValue(body)
                .retrieve()
                .bodyToMono<PoppOpptjeningsgrunnlagResult>()
                .retryWhen(retryBackoffSpec(url))
                .block()
                ?.toInternalValue()
                .also { countCalls(MetricResult.OK) }
                ?: OpptjeningsgrunnlagSamling.empty(spec.pid)
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun fetchFreshPensjonspoeng(spec: PensjonspoengSpec): List<Opptjeningsgrunnlag> {
        val url = "$baseUrl/$PENSJONSPOENG_PATH"
        log.debug { "POST to URL: '$url'" }
        val body = PoppPensjonspoengSpec.fromInternalValue(spec)

        return try {
            webClient
                .post()
                .uri("/$PENSJONSPOENG_PATH")
                .headers(::setHeaders)
                .bodyValue(body)
                .retrieve()
                .bodyToMono<PoppPensjonspoengResult>()
                .retryWhen(retryBackoffSpec(url))
                .block()
                ?.toInternalValue()
                .also { countCalls(MetricResult.OK) }
                ?: emptyList()
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun setHeadersForGet(headers: HttpHeaders, pid: Pid) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
        headers[CustomHttpHeaders.PID] = pid.value
    }

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    private fun zeroInntekt() =
        LoependeInntekt(
            aarligBeloep = 0,
            fom = LocalDate.of(time.today().year, 1, 1)
        )

    companion object {
        private const val BEHOLDNING_PATH = "popp/api/beholdning"
        private const val OPPTJENINGSGRUNNLAG_GET_PATH = "popp/api/opptjeningsgrunnlag"
        private const val OPPTJENINGSGRUNNLAG_PATH = "popp/api/opptjeningsgrunnlag/hent"
        private const val PENSJONSPOENG_PATH = "popp/api/pensjonspoeng/hent"
        private val service = EgressService.PENSJONSOPPTJENING
    }
}