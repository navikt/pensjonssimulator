package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025Service
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.OpptjeningsperiodeService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseMapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.TpForholdDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
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
        spec: TjenestepensjonSimuleringPre2025Spec,
        stillingsprosentListe: List<Stillingsprosent>,
        tpOrdning: TpOrdningFullDto,
    ): SimulerOffentligTjenestepensjonResultV1 {
        val request = HentPrognoseMapper.toDto(spec)
        val opptjeningsperiodeResponse = opptjeningsperiodeService.getOpptjeningsperiodeListe(tpOrdning, stillingsprosentListe)

        request.tpForholdListe = buildTpForhold(opptjeningsperiodeResponse.tpOrdningOpptjeningsperiodeMap)
        request.sisteTpnr = tpOrdning.tpNr
        val requestWithFilteredFnr = TjenestepensjonSimuleringPre2025Service.filterFnr(request.toString())
        log.debug { "Populated request: $requestWithFilteredFnr" }
        log.debug { "Populated request JSON: ${objectMapper.writeValueAsString(request)}" } //OBS: request logges som debug i dev, fnr må maskeres for logging i prod
        return try {
            spkTjenestepensjonClientPre2025.getPrognose(request = request, tpOrdning = tpOrdning)
        } catch (e: WebClientResponseException) {
            val rawResponseBody = e.responseBodyAsString
            val responseBody = e.responseBodyAsString.let { StringUtils.replace(it, "Ã¥", "å") }
                .let { StringUtils.replace(it, "Ã\u0083Â¥", "å") }
                .let { StringUtils.replace(it, "Ã¦", "æ") }
                .let { StringUtils.replace(it, "Ã¸", "ø") }
                .let { StringUtils.replace(it, "Ã\u0083Â¸", "ø") }

            log.warn(e) { "Error <$responseBody> while calling SPK with request: $requestWithFilteredFnr" }
            log.warn { "Raw responseBody <$rawResponseBody> while calling SPK with request: $requestWithFilteredFnr" }
            if (responseBody.contains("Validation problem")) {
                throw BrukerKvalifisererIkkeTilTjenestepensjonException(responseBody)
            }
            throw e
        }
    }

    private fun buildTpForhold(tpOrdningOpptjeningsperiodeMap: Map<TpOrdningFullDto, List<OpptjeningsperiodeDto>>) =
        tpOrdningOpptjeningsperiodeMap.map { entry -> TpForholdDto(entry.key.tpNr, entry.value) }

}