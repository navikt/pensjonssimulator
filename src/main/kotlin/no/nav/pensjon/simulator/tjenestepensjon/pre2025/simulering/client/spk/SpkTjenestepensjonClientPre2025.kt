package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk

import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.TjenestepensjonClientPre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.HentPrognoseRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.HentPrognoseResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.PrognoseResultMapper.fromDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.PrognoseSpecMapper.toDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class SpkTjenestepensjonClientPre2025(
    @Value($$"${spk.tp-simulering.pre-2025.url}") baseUrl: String,
    @Value($$"${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    private val traceAid: TraceAid,
    private val sporingslogger: SporingsloggService,
) : ExternalServiceClient(retryAttempts), TjenestepensjonClientPre2025 {

    private val webClient = webClientBase.withBaseUrl(baseUrl)

    override fun getPrognose(
        spec: TjenestepensjonSimuleringPre2025Spec,
        tpOrdning: TpOrdningFullDto
    ): SimulerOffentligTjenestepensjonResult {
        val request: HentPrognoseRequestDto = toDto(spec)

        sporingslogger.logUtgaaendeRequest(
            organisasjonsnummer = Organisasjoner.SPK,
            pid = spec.pid,
            leverteData = request.toString()
        )

        val response: HentPrognoseResponseDto? = webClient
            .post()
            .uri(PATH)
            .headers(::setHeaders)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<HentPrognoseResponseDto>()
            .block()

        return response?.let(::fromDto)
            ?: fromDto(HentPrognoseResponseDto(tpnr = request.sisteTpnr, navnOrdning = tpOrdning.tpNr))
    }

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    companion object {
        private const val PATH = "/nav/pensjon/prognose/v1"
        private val service = EgressService.SPK
    }
}