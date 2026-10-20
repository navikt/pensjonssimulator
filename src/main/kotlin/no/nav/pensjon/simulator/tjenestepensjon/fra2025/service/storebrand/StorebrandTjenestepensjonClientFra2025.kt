package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.StorebrandMapper.fromResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.StorebrandMapper.toRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandSimulerTjenestepensjonResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Service("storebrand")
class StorebrandTjenestepensjonClientFra2025(
    @Value($$"${storebrand.tp-simulering.fra-2025.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    @Qualifier("long-timeout") webClientBase: WebClientBase,
    private val traceAid: TraceAid,
    private val sporingslogg: SporingsloggService,
    private val sammenligner: SammenlignAFPService
) : ExternalServiceClient(retryAttempts), TjenestepensjonFra2025Client {

    override val service = service()
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)

    override fun simuler(
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        tpNummer: String
    ): Result<SimulertTjenestepensjon> {
        val request: StorebrandSimulerTjenestepensjonRequest = toRequestDto(spec)
        log.debug { "Simulating tjenestepensjon 2025 with ${service.shortName} with request $request" }

        sporingslogg.logUtgaaendeRequest(
            organisasjonsnummer = Organisasjoner.STOREBRAND,
            pid = spec.pid,
            leverteData = request.toString()
        )

        return try {
            webClient
                .post()
                .uri("/$tpNummer/$SIMULER_PATH")
                .bodyValue(request)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono<StorebrandSimulerTjenestepensjonResponse>()
                .block()
                ?.let { success(spec, request, response = it) }
                ?: Result.failure(TjenestepensjonSimuleringException("No response body", service.shortName))
        } catch (e: WebClientResponseException) {
            "Failed to simulate tjenestepensjon 2025 with ${service.shortName} ${e.responseBodyAsString}".let {
                log.error(e) { it }
                Result.failure(TjenestepensjonSimuleringException(it, service.shortName))
            }
        } catch (e: WebClientRequestException) {
            "Failed to send request to simulate tjenestepensjon 2025 with ${service.shortName}".let {
                log.error(e) { "$it med url ${e.uri}" }
                Result.failure(TjenestepensjonSimuleringException(it, service.shortName))
            }
        } catch (e: EgressException) {
            "Failed to simulate tjenestepensjon 2025 at ${service.shortName}".let {
                log.error(e) { "$it med url $SIMULER_PATH/$tpNummer - status: ${e.statusCode}" }
                Result.failure(TjenestepensjonSimuleringException(it, service.shortName))
            }
        }
    }

    private fun success(
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        request: StorebrandSimulerTjenestepensjonRequest,
        response: StorebrandSimulerTjenestepensjonResponse
    ): Result<SimulertTjenestepensjon> =
        Result.success(
            fromResponseDto(response, request)
                .also { sammenligner.sammenlignOgLoggAfp(spec, it.utbetalingsperioder) }
        )

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun service() = EgressService.STOREBRAND

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    companion object {
        private const val SIMULER_PATH = "ofa/v1/tjenestepensjon/simuler"
    }
}