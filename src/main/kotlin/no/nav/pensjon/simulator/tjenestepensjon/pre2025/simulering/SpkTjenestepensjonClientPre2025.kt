package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.FnrDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.SivilstandCodeEnumDto
import no.nav.pensjon.simulator.tpregisteret.acl.TpOrdningFullDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate

@Service
class SpkTjenestepensjonClientPre2025(
    @Value("\${ps.pen.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
    private val sporingsloggService: SporingsloggService,
) : ExternalServiceClient(retryAttempts) {
    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    fun getResponse(request: HentPrognoseRequestDto, tpOrdning: TpOrdningFullDto): HentPrognoseResponseDto {
        sporingsloggService.log(Pid(request.fnr.fnr), request.toString(), "")
        val response: HentPrognoseResponseDto? = webClient
            .post()
            .uri(PATH)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<HentPrognoseResponseDto>()
            .block()
        return response
            ?: HentPrognoseResponseDto(request.sisteTpnr, tpOrdning.tpNr)
    }

    fun ping(): String = webClient
        .post()
        .uri(PATH)
        .headers(::setHeaders)
        .bodyValue(dummyRequest())
        .retrieve()
        .bodyToMono(String::class.java)
        .block() ?: "No body received"

    private fun dummyRequest(fnr: String = "01015512345") = HentPrognoseRequestDto(
        fnr = FnrDto(fnr),
        fodselsdato = LocalDate.of(1955, 1, 1),
        sisteTpnr = "3010",
        sivilstandkode = SivilstandCodeEnumDto.UGIF,
        inntektListe = emptyList(),
        simuleringsperiodeListe = emptyList(),
        simuleringsdataListe = emptyList()
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
        private const val PATH = "/nav/pensjon/prognose/v1"
        private val service = EgressService.SPK
    }
}