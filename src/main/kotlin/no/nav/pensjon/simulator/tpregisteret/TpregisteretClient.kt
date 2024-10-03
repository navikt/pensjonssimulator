package no.nav.pensjon.simulator.tpregisteret

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class TpregisteretClient(
    @Value("\${tjenestepensjon.url}") baseUrl: String,
    @Value("\${ps.web-client.retry-attempts}") retryAttempts: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid
) : ExternalServiceClient(retryAttempts) {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    fun hentErBrukerTilknyttetTpLeverandoer(pid: String, orgNummer: String): Boolean {
        val uri = "$PATH/hasForhold?orgnr=$orgNummer"
        return try {
            val response = webClient.post()
                .uri(uri)
                .bodyValue(pid)
                .headers { setHeaders(it) }
                .retrieve()
                .bodyToMono(BrukerTilknyttetTpLeverandoerResponse::class.java)
                .block()
            response?.forhold ?: logEnReturnFalse("Tom responsebody fra tpregisteret", orgNummer)
        } catch (e: WebClientRequestException) {
            logEnReturnFalse("Failed calling $uri", orgNummer, e)
        } catch (e: WebClientResponseException) {
            logEnReturnFalse(e.responseBodyAsString, orgNummer, e)
        } catch (e: Exception) {
            logEnReturnFalse("Unexpected exception ${e.message}, while calling tpregisteret", orgNummer, e)
        }
    }

    private fun logEnReturnFalse(feilmelding: String, orgNummer: String, e: Exception? = null): Boolean {
        val errorMsg = "Teknisk feil ved verifisering av at brukeren er tilknyttet angitt tp-leverandør med orgnummer $orgNummer: $feilmelding"
        e?.let { log.error(e) { errorMsg } } ?: log.error { errorMsg }
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
        private val service = EgressService.TP_REGISTERET
    }
}