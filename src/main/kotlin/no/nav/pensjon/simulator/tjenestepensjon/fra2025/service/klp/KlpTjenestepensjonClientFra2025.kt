package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import io.netty.handler.timeout.ReadTimeoutHandler
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.env.EnvironmentUtil
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.mapToRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.mapToResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Utbetaling
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient

@Service
class KlpTjenestepensjonClientFra2025(
    @Value("\${klp.tp-simulering.fra-2025.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
    private val sporingslogg: SporingsloggService,
    private val sammenligner: SammenlignAFPService,
    private val isDevelopment: () -> Boolean = { EnvironmentUtil.isDevelopment() },
) : ExternalServiceClient(retryAttempts), TjenestepensjonFra2025Client {
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl)
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient
                    .create()
                    .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(ON_CONNECTED_READ_TIMEOUT_SECONDS)) })
        ).build()

    override fun simuler(
        spec: SimulerOffentligTjenestepensjonFra2025SpecV1,
        tpNummer: String
    ): Result<SimulertTjenestepensjon> {
        val request: KlpSimulerTjenestepensjonRequest = mapToRequest(spec)

        if (isDevelopment())
            return success(request, spec, mockResponse(spec))

        sporingslogg.logUtgaaendeRequest(Organisasjoner.KLP, Pid(spec.pid), request.toString())

        val response = try {
            webClient
                .post()
                .uri("$SIMULER_PATH/$tpNummer")
                .bodyValue(request)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono<KlpSimulerTjenestepensjonResponse>()
                .block()
        } catch (e: WebClientResponseException) {
            "Failed to simulate tjenestepensjon 2025 at ${service.shortName} ${e.responseBodyAsString}".let {
                log.error(e) { it }
                return Result.failure(TjenestepensjonSimuleringException(it))
            }
        } catch (e: WebClientRequestException) {
            "Failed to send request to simulate tjenestepensjon 2025 at ${service.shortName}".let {
                log.error(e) { "$it med url ${e.uri}" }
                return Result.failure(TjenestepensjonSimuleringException(it))
            }
        } catch (e: EgressException) {
            "Failed to simulate tjenestepensjon 2025 at ${service.shortName}".let {
                log.error(e) { "$it med url $SIMULER_PATH/$tpNummer - status: ${e.statusCode}" }
                return Result.failure(TjenestepensjonSimuleringException(it))
            }
        }

        return response?.let { success(request, spec, it) }
            ?: Result.failure(TjenestepensjonSimuleringException("No response body"))
    }

    private fun success(
        request: KlpSimulerTjenestepensjonRequest,
        spec: SimulerOffentligTjenestepensjonFra2025SpecV1,
        response: KlpSimulerTjenestepensjonResponse
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
        private const val ON_CONNECTED_READ_TIMEOUT_SECONDS = 45

        private fun mockResponse(spec: SimulerOffentligTjenestepensjonFra2025SpecV1) =
            KlpSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning("3100")),
                utbetalingsListe = listOf(
                    Utbetaling(
                        fraOgMedDato = spec.uttaksdato,
                        manedligUtbetaling = 3576,
                        arligUtbetaling = 42914,
                        ytelseType = TjenestepensjonYtelseType.PAASLAG.kode
                    ),
                    Utbetaling(
                        fraOgMedDato = spec.uttaksdato.plusYears(5),
                        manedligUtbetaling = 2232,
                        arligUtbetaling = 26779,
                        ytelseType = TjenestepensjonYtelseType.ALDERSPENSJON_OPPTJENT_FOER_2020.kode
                    ),
                    Utbetaling(
                        fraOgMedDato = spec.uttaksdato,
                        manedligUtbetaling = 884,
                        arligUtbetaling = 10609,
                        ytelseType = TjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.kode
                    ),
                ),
                arsakIngenUtbetaling = emptyList(),
                betingetTjenestepensjonErInkludert = false
            )
    }
}
