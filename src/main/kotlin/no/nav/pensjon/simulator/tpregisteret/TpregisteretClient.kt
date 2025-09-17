package no.nav.pensjon.simulator.tpregisteret

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tpregisteret.acl.BrukerTilknyttetTpLeverandoerResponse
import no.nav.pensjon.simulator.tpregisteret.acl.HentAlleTPForholdResponseDto
import no.nav.pensjon.simulator.tpregisteret.acl.TpForhold
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono

@Component
class TpregisteretClient(
    @Value("\${tjenestepensjon.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts) {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    fun hentErBrukerTilknyttetTpLeverandoer(pid: Pid, organisasjonsnummer: Organisasjonsnummer): Boolean {
        val uri = "$PATH/hasForhold?orgnr=${organisasjonsnummer.value}"
        return try {
            val response = webClient.post()
                .uri(uri)
                .bodyValue(pid.value)
                .headers(::setHeaders)
                .retrieve()
                .bodyToMono(BrukerTilknyttetTpLeverandoerResponse::class.java)
                .block()
            response?.forhold ?: logAndReturnFalse("Tom responsebody fra ${service.shortName}", organisasjonsnummer)
        } catch (e: WebClientRequestException) {
            logAndReturnFalse("Failed calling $uri", organisasjonsnummer, e)
        } catch (e: WebClientResponseException) {
            logAndReturnFalse(e.responseBodyAsString, organisasjonsnummer, e)
        } catch (e: Exception) {
            logAndReturnFalse(
                "Unexpected exception ${e.message}, while calling ${service.shortName}",
                organisasjonsnummer,
                e
            )
        }
    }

    fun findAlleTPForhold(fnr: String): List<TpForhold> {
        return webClient.get()
            .uri(FORHOLD_PATH)
            .headers { setHeaders(it); it["fnr"] = fnr }
            .exchangeToMono(::handleAlleTpForholdResponse)
            .block()
            .orEmpty()
    }

    fun findTssId(tpId: String): String? {
        return try {
            webClient.get()
                .uri("$TSS_PATH$tpId")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        } catch (_: WebClientResponseException.NotFound) {
            null
        }
    }

    private fun handleAlleTpForholdResponse(response: ClientResponse): Mono<List<TpForhold>> =
        when (response.statusCode()) {
            HttpStatus.OK -> response.bodyToMono<HentAlleTPForholdResponseDto>().map {
                it.forhold.map { forhold ->
                    TpForhold(
                        tpNr = forhold.tpNr,
                        navn = forhold.tpOrdningNavn ?: forhold.tpNr,
                        datoSistOpptjening = forhold.datoSistOpptjening
                    )
                }
            }

            HttpStatus.NOT_FOUND -> Mono.empty()
            else -> Mono.error(RuntimeException("Error fetching data from TP: Received status code ${response.statusCode()} fra tpregisteret"))
        }

    private fun logAndReturnFalse(
        feilmelding: String,
        organisasjonsnummer: Organisasjonsnummer,
        e: Exception? = null
    ): Boolean {
        val message = "Teknisk feil ved verifisering av at brukeren er tilknyttet tjenestepensjonsordning" +
                " med organisasjonsnummer $organisasjonsnummer: $feilmelding"
        e?.let { log.error(e) { message } } ?: log.error { message }
        return false
    }

    override fun service() = service

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        with(EgressAccess.token(service).value) {
            headers.setBearerAuth(this)
            log.debug { "Token: $this" }
        }

        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val PATH = "/api/tjenestepensjon"
        private const val FORHOLD_PATH = "/api/intern/tjenestepensjon/forhold/"
        private const val TSS_PATH = "/api/tpconfig/tssnr/"
        private val service = EgressService.TP_REGISTERET
    }
}
