package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner.SPK
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseMapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseResponseDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class SpkTjenestepensjonClientPre2025(
    @Value("\${spk.tp-simulering.pre-2025.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBase: WebClientBase,
    private val traceAid: TraceAid,
    private val sporingsloggService: SporingsloggService,
) : ExternalServiceClient(retryAttempts) {
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBase.withBaseUrl(baseUrl)

    fun getPrognose(request: HentPrognoseRequestDto, tpOrdning: TpOrdningFullDto): SimulerOffentligTjenestepensjonResultV1 {
        sporingsloggService.logUtgaaendeRequest(SPK, Pid(request.fnr), request.toString())
        val response: HentPrognoseResponseDto? = webClient
            .post()
            .uri(PATH)
            .headers(::setHeaders)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<HentPrognoseResponseDto>()
            .block()
        return response?.let { HentPrognoseMapper.fromDto(it) }
            ?: HentPrognoseMapper.fromDto(HentPrognoseResponseDto(request.sisteTpnr, tpOrdning.tpNr))
    }

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
        private const val PATH = "/nav/pensjon/prognose/v1"
        private val service = EgressService.SPK
    }
}