package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

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
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KLPMapper.mapToRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KLPMapper.mapToResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KLPSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KLPSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Utbetaling
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class KLPTjenestepensjonClientFra2025(
    @Value("\${klp.tp-simulering.fra-2025.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
    private val sporingslogg: SporingsloggService,
    private val sammenligner: SammenlignAFPService,
) : ExternalServiceClient(retryAttempts), TjenestepensjonFra2025Client {
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun simuler(spec: SimulerTjenestepensjonRequestDto, tpNummer: String): Result<SimulertTjenestepensjon> {
        val request: KLPSimulerTjenestepensjonRequest = mapToRequest(spec)
        val response = if (System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp") {
            provideMockResponse(spec)
        } else {
            sporingslogg.logUtgaaendeRequest(Organisasjoner.KLP, Pid(spec.pid), request.toString())

            try {
                webClient
                    .post()
                    .uri("$SIMULER_PATH/$tpNummer")
                    .bodyValue(request)
                    .headers(::setHeaders)
                    .retrieve()
                    .bodyToMono<KLPSimulerTjenestepensjonResponse>()
                    .block()
            } catch (e: WebClientResponseException) {
                "Failed to simulate tjenestepensjon 2025 hos ${service.shortName} ${e.responseBodyAsString}".let {
                    log.error(e) { it }
                    return Result.failure(TjenestepensjonSimuleringException(it))
                }
            } catch (e: WebClientRequestException) {
                "Failed to send request to simulate tjenestepensjon 2025 hos ${service.shortName}".let {
                    log.error(e) { "$it med url ${e.uri}" }
                    return Result.failure(TjenestepensjonSimuleringException(it))
                }
            }
        }
        return response?.let { success(request, spec, it) }
            ?: Result.failure(TjenestepensjonSimuleringException("No response body"))
    }

    private fun success(
        request: KLPSimulerTjenestepensjonRequest,
        spec: SimulerTjenestepensjonRequestDto,
        response: KLPSimulerTjenestepensjonResponse
    ): Result<SimulertTjenestepensjon> =
        Result.success(
            mapToResponse(response, request)
                .also { sammenligner.sammenlignOgLoggAfp(spec, it.utbetalingsperioder) })

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
        private const val SIMULER_PATH = "/api/oftp/simulering"
        private val service = EgressService.KLP

        fun provideMockResponse(spec: SimulerTjenestepensjonRequestDto) =
            KLPSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning("3100")),
                utbetalingsListe = listOf(
                    Utbetaling(fraOgMedDato = spec.uttaksdato, manedligUtbetaling = 3576, arligUtbetaling = 42914, ytelseType = "PAASLAG"),
                    Utbetaling(fraOgMedDato = spec.uttaksdato.plusYears(5), manedligUtbetaling = 2232, arligUtbetaling = 26779, ytelseType = "APOF2020"),
                    Utbetaling(fraOgMedDato = spec.uttaksdato, manedligUtbetaling = 884, arligUtbetaling = 10609, ytelseType = "BTP"),
                ),
                arsakIngenUtbetaling = emptyList(),
                betingetTjenestepensjonErInkludert = false,
            )
    }
}
