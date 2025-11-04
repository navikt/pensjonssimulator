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
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SpkMapper.mapToRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SpkMapper.mapToResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkSimulerTjenestepensjonResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class SpkTjenestepensjonClientFra2025(
    @Value("\${spk.tp-simulering.fra-2025.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    @Qualifier("long-timeout") webClientBase: WebClientBase,
    private val traceAid: TraceAid,
    private val sporingslogg: SporingsloggService,
    private val sammenligner: SammenlignAFPService
) : ExternalServiceClient(retryAttempts), TjenestepensjonFra2025Client {
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)

    override fun simuler(spec: SimulerOffentligTjenestepensjonFra2025SpecV1, tpNummer: String): Result<SimulertTjenestepensjon> {
        val request: SpkSimulerTjenestepensjonRequest = mapToRequest(spec)
        log.debug { "Simulating tjenestepensjon 2025 with ${service.shortName} with request $request" }
        sporingslogg.logUtgaaendeRequest(Organisasjoner.SPK, Pid(spec.pid), request.toString())

        return try {
            webClient
                .post()
                .uri("$SIMULER_PATH/$tpNummer")
                .bodyValue(request)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono<SpkSimulerTjenestepensjonResponse>()
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
        } catch (e: EgressException) {
            "Failed to simulate tjenestepensjon 2025 at ${service.shortName}".let {
                log.error(e) { "$it med url $SIMULER_PATH/$tpNummer - status: ${e.statusCode}" }
                return Result.failure(TjenestepensjonSimuleringException(it))
            }
        }
    }

    private fun success(
        spec: SimulerOffentligTjenestepensjonFra2025SpecV1,
        request: SpkSimulerTjenestepensjonRequest,
        response: SpkSimulerTjenestepensjonResponse
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
