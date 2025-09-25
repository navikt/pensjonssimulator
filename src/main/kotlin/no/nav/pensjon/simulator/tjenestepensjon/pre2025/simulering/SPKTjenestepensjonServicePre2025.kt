package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Service
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.TpForholdDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.acl.TpOrdningFullDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.OpptjeningsperiodeService
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class SPKTjenestepensjonServicePre2025(
    private val spkTjenestepensjonClientPre2025: SpkTjenestepensjonClientPre2025,
    private val opptjeningsperiodeService: OpptjeningsperiodeService,
    private val objectMapper: ObjectMapper
) {
    private val log = KotlinLogging.logger {}

    fun simulerOffentligTjenestepensjon(
        request: HentPrognoseRequestDto,
        stillingsprosentListe: List<Stillingsprosent>,
        tpOrdning: TpOrdningFullDto,
    ): HentPrognoseResponseDto {
        val opptjeningsperiodeResponse = opptjeningsperiodeService.getOpptjeningsperiodeListe(tpOrdning, stillingsprosentListe)

        request.tpForholdListe = buildTpForhold(opptjeningsperiodeResponse.tpOrdningOpptjeningsperiodeMap)
        request.sisteTpnr = tpOrdning.tpNr
        val requestWithFilteredFnr = TjenestepensjonSimuleringPre2025Service.Companion.filterFnr(request.toString())
        log.debug { "Populated request: $requestWithFilteredFnr" }
        log.debug { "Populated request JSON: ${objectMapper.writeValueAsString(request)}" } //OBS: request logges som debug i dev, fnr må maskeres for logging i prod
        return try {
            spkTjenestepensjonClientPre2025.getPrognose(request = request, tpOrdning = tpOrdning)
        } catch (e: WebClientResponseException) {
            val responseBody = e.responseBodyAsString.let { StringUtils.replace(it, "Ã¥", "å") }
                .let { StringUtils.replace(it, "Ã\u0083Â¥", "å") }
                .let { StringUtils.replace(it, "Ã¦", "æ") }
                .let { StringUtils.replace(it, "Ã¸", "ø") }
                .let { StringUtils.replace(it, "Ã\u0083Â¸", "ø") }

            log.warn(e) { "Error <$responseBody> while calling SPK with request: $requestWithFilteredFnr" }
            if (responseBody.contains("Validation problem")) {
                throw BrukerKvalifisererIkkeTilTjenestepensjonException(responseBody)
            }
            throw e
        }
    }

    private fun buildTpForhold(tpOrdningOpptjeningsperiodeMap: Map<TpOrdningFullDto, List<OpptjeningsperiodeDto>>) =
        tpOrdningOpptjeningsperiodeMap.map { entry -> TpForholdDto(entry.key.tpNr, entry.value) }

}