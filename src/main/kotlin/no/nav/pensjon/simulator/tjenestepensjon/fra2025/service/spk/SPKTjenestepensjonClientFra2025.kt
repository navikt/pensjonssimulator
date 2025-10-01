package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SPKMapper.mapToRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SPKMapper.mapToResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SPKSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SPKSimulerTjenestepensjonResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class SPKTjenestepensjonClientFra2025(
    @Value("\${spk.tp-simulering.fra-2025.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
    private val sporingslogg: SporingsloggService,
    private val sammenligner: SammenlignAFPService
) : ExternalServiceClient(retryAttempts), TjenestepensjonFra2025Client {
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun simuler(spec: SimulerTjenestepensjonRequestDto, tpNummer: String): Result<SimulertTjenestepensjon> {
        val request: SPKSimulerTjenestepensjonRequest = mapToRequest(spec)
        log.debug { "Simulating tjenestepensjon 2025 with ${service.shortName} with request $request" }
        sporingslogg.logUtgaaendeRequest(Organisasjoner.SPK, Pid(spec.pid), request.toString())

        return try {
            webClient
                .post()
                .uri("$SIMULER_PATH/$tpNummer")
                .bodyValue(request)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono<SPKSimulerTjenestepensjonResponse>()
                .block()
                ?.let { success(spec, request, response = it) }
                ?: Result.failure(TjenestepensjonSimuleringException("No response body"))
        } catch (e: WebClientResponseException) {
            "Failed to simulate tjenestepensjon 2025 with ${service.shortName} ${e.responseBodyAsString}".let {
                log.error(e) { it }
                Result.failure(TjenestepensjonSimuleringException(it))
            }
        } catch (e: WebClientRequestException) {
            "Failed to send request to simulate tjenestepensjon 2025 with ${service.shortName}".let {
                log.error(e) { "$it med url ${e.uri}" }
                Result.failure(TjenestepensjonSimuleringException(it))
            }
        }
    }

    private fun success(
        spec: SimulerTjenestepensjonRequestDto,
        request: SPKSimulerTjenestepensjonRequest,
        response: SPKSimulerTjenestepensjonResponse
    ): Result<SimulertTjenestepensjon> =
        Result.success(
            mapToResponse(response, request)
                .also { sammenligner.sammenlignOgLoggAfp(spec, it.utbetalingsperioder) }
        )

    private fun setHeaders(headers: HttpHeaders) {
        with(EgressAccess.token(service).value) {
            headers.setBearerAuth(this)
            log.debug { "Token: $this" }
        }

        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    companion object {
        private const val SIMULER_PATH = "/nav/v2/tjenestepensjon/simuler"
        private val service = EgressService.SPK
    }
}
