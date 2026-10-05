package no.nav.pensjon.simulator.opptjening.client.popp

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
import no.nav.pensjon.simulator.tech.metric.MetricResult
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class PoppOpptjeningClient(
    @param:Value($$"${ps.popp.url}") private val baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    private val cache: PoppOpptjeningCache,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts), OpptjeningClient {

    private val webClient = webClientBase.withBaseUrl(baseUrl)
    private val log = KotlinLogging.logger {}

    override fun fetchBeholdninger(spec: BeholdningSpec): List<Pensjonsbeholdning> =
        cache.cachedOrFreshBeholdninger(spec, fetcher = ::fetchFreshBeholdninger).toInternalValue()

    override fun fetchOpptjeningsgrunnlag(spec: OpptjeningsgrunnlagSpec): OpptjeningsgrunnlagSamling? =
        cache.cachedOrFreshOpptjeningsgrunnlag(spec, fetcher = ::fetchFreshOpptjeningsgrunnlag)
            .toInternalValue()

    override fun fetchPensjonspoeng(spec: PensjonspoengSpec): List<Opptjeningsgrunnlag> =
        cache.cachedOrFreshPensjonspoeng(spec, fetcher = ::fetchFreshPensjonspoeng).toInternalValue()

    override fun fetchSistLignedeInntekt(pid: Pid): LoependeInntekt? =
        cache.cachedOrFreshOpptjeningsgrunnlag(
            spec = OpptjeningsgrunnlagSpec(pid), fetcher = ::fetchFreshOpptjeningsgrunnlag
        ).toLoependeInntekt()

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun fetchFreshBeholdninger(spec: BeholdningSpec): PoppBeholdningResult? {
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
                .also { countCalls(MetricResult.OK) }
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun fetchFreshOpptjeningsgrunnlag(spec: OpptjeningsgrunnlagSpec): PoppOpptjeningsgrunnlagResult? {
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
                .also { countCalls(MetricResult.OK) }
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun fetchFreshPensjonspoeng(spec: PensjonspoengSpec): PoppPensjonspoengResult? {
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
                .also { countCalls(MetricResult.OK) }
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $url", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val BEHOLDNING_PATH = "popp/api/beholdning"
        private const val OPPTJENINGSGRUNNLAG_PATH = "popp/api/opptjeningsgrunnlag/hent"
        private const val PENSJONSPOENG_PATH = "popp/api/pensjonspoeng/hent"
        private val service = EgressService.PENSJONSOPPTJENING
    }
}