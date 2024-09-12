package no.nav.pensjon.simulator.uttak.client.pen

import mu.KotlinLogging
import no.nav.pensjon.simulator.common.client.ExternalServiceClient
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.uttak.*
import no.nav.pensjon.simulator.uttak.client.UttakClient
import no.nav.pensjon.simulator.uttak.client.pen.acl.PenTidligstMuligUttakResult
import no.nav.pensjon.simulator.uttak.client.pen.acl.PenUttakSpecMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenUttakClient(
    @Value("\${ps.pen.url}") baseUrl: String,
    webClientBuilder: WebClient.Builder,
    private val traceAid: TraceAid,
) : ExternalServiceClient("0"), UttakClient {

    private val log = KotlinLogging.logger {}
    private val webClient = webClientBuilder.baseUrl(baseUrl).build()

    override fun service() = service

    override fun finnTidligstMuligUttak(spec: TidligstMuligUttakSpec): TidligstMuligUttak {
        val uri = "$BASE_PATH/$PATH"
        val dto = PenUttakSpecMapper.toDto(spec)
        log.debug { "POST to URI: '$uri' with body '$dto'" }

        return try {
            webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(::setHeaders)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PenTidligstMuligUttakResult::class.java)
                .retryWhen(retryBackoffSpec(uri))
                .block()
                ?.let { enrichedResult(it, spec.gradertUttak) }
                ?: nullResult(spec.gradertUttak)
        } catch (e: WebClientRequestException) {
            throw EgressException("Failed calling $uri", e)
        } catch (e: WebClientResponseException) {
            throw EgressException(e.responseBodyAsString, e)
        }
    }

    override fun toString(e: EgressException, uri: String) = "Failed calling $uri"

    private fun setHeaders(headers: HttpHeaders) {
        headers.setBearerAuth(EgressAccess.token(service).value)
        headers[CustomHttpHeaders.CALL_ID] = traceAid.callId()
    }

    companion object {
        private const val BASE_PATH = "pen/api"
        private const val PATH = "ekstern/simulering/v1/tidligst-mulig-uttak"

        private val service = EgressService.PENSJONSFAGLIG_KJERNE

        private fun enrichedResult(result: PenTidligstMuligUttakResult, uttakSpec: GradertUttakSpec?) =
            TidligstMuligUttak(
                uttakDato = result.dato,
                uttakGrad = uttakGrad(uttakSpec)
            )

        private fun nullResult(uttakSpec: GradertUttakSpec?) =
            TidligstMuligUttak(
                uttakGrad = uttakGrad(uttakSpec),
                feil = TidligstMuligUttakFeil(type = TidligstMuligUttakFeilType.NONE, beskrivelse = "null")
            )

        private fun uttakGrad(spec: GradertUttakSpec?) = spec?.grad ?: UttakGrad.HUNDRE_PROSENT
    }
}
