package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025Service
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.OpptjeningsperiodeService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseMapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.TpForholdDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

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
    ): SimulerOffentligTjenestepensjonResult {
        val request = HentPrognoseMapper.toDto(spec)
        val opptjeningsperiodeResponse = opptjeningsperiodeService.getOpptjeningsperiodeListe(tpOrdning, stillingsprosentListe)

        request.tpForholdListe = buildTpForhold(opptjeningsperiodeResponse.tpOrdningOpptjeningsperiodeMap)
        request.sisteTpnr = tpOrdning.tpNr
        val requestWithFilteredFnr = TjenestepensjonSimuleringPre2025Service.filterFnr(request.toString())
        log.debug { "Populated request: $requestWithFilteredFnr" }
        log.debug { "Populated request JSON: ${objectMapper.writeValueAsString(request)}" } //OBS: request logges som debug i dev, fnr må maskeres for logging i prod
        return spkTjenestepensjonClientPre2025.getPrognose(request = request, tpOrdning = tpOrdning)
    }

    private fun buildTpForhold(tpOrdningOpptjeningsperiodeMap: Map<TpOrdningFullDto, List<OpptjeningsperiodeDto>>) =
        tpOrdningOpptjeningsperiodeMap.map { entry -> TpForholdDto(entry.key.tpNr, entry.value) }

}