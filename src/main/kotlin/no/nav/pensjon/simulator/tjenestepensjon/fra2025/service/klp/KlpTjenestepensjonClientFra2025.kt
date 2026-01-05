package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.env.EnvironmentUtil
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.toRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.fromResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Utbetaling
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Service("klp")
class KlpTjenestepensjonClientFra2025(
    @Value("\${klp.tp-simulering.fra-2025.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    @Qualifier("long-timeout") webClientBase: WebClientBase,
    private val traceAid: TraceAid,
    private val sporingslogg: SporingsloggService,
    private val sammenligner: SammenlignAFPService,
    private val isDevelopment: () -> Boolean = { EnvironmentUtil.isDevelopment() },
) : ExternalServiceClient(retryAttempts), TjenestepensjonFra2025Client {
    override val leverandoerKortNavn = service.shortName
    override val leverandoerFulltNavn = service.description
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)

    override fun simuler(
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        tpNummer: String
    ): Result<SimulertTjenestepensjon> {
        val request: KlpSimulerTjenestepensjonRequest = toRequestDto(spec)

        if (isDevelopment())
            return success(request, spec, mockResponse(spec))

        sporingslogg.logUtgaaendeRequest(Organisasjoner.KLP, spec.pid, request.toString())

        return try {
            webClient
                .post()
                .uri("$SIMULER_PATH/$tpNummer")
                .bodyValue(request)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono<KlpSimulerTjenestepensjonResponse>()
                .block()
                ?.let { success(request, spec, it) }
                ?: Result.failure(TjenestepensjonSimuleringException("No response body"))
        } catch (e: WebClientResponseException) {
            "Failed to simulate tjenestepensjon 2025 at ${service.shortName} ${e.responseBodyAsString}".let {
                log.error(e) { it }
                Result.failure(TjenestepensjonSimuleringException(it))
            }
        } catch (e: WebClientRequestException) {
            "Failed to send request to simulate tjenestepensjon 2025 at ${service.shortName}".let {
                log.error(e) { "$it med url ${e.uri}" }
                Result.failure(TjenestepensjonSimuleringException(it))
            }
        } catch (e: EgressException) {
            "Failed to simulate tjenestepensjon 2025 at ${service.shortName}".let {
                log.error(e) { "$it med url $SIMULER_PATH/$tpNummer - status: ${e.statusCode}" }
                Result.failure(TjenestepensjonSimuleringException(it))
            }
        }
    }

    private fun success(
        request: KlpSimulerTjenestepensjonRequest,
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        response: KlpSimulerTjenestepensjonResponse
    ): Result<SimulertTjenestepensjon> =
        Result.success(
            fromResponseDto(response, request)
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

        private fun mockResponse(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
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
