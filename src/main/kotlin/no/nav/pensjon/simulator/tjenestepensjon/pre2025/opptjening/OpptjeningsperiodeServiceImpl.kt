package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.error.DuplicateOpptjeningsperiodeEndDateException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.error.MissingOpptjeningsperiodeException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.acl.TpOrdningFullDto
import no.nav.tjenestepensjon.simulering.v2.service.OpptjeningsperiodeResponse
import org.springframework.stereotype.Service

@Service
class OpptjeningsperiodeServiceImpl: OpptjeningsperiodeService {

    override fun getOpptjeningsperiodeListe(tpOrdning: TpOrdningFullDto, stillingsprosentListe: List<Stillingsprosent>) =
        OpptjeningsperiodeResponse(
            mapOf(tpOrdning to mapStillingsprosentToOpptjeningsperiodeList(stillingsprosentListe)),
            emptyList()
        )

    @Throws(
            DuplicateOpptjeningsperiodeEndDateException::class,
            MissingOpptjeningsperiodeException::class
    )
    override fun getLatestFromOpptjeningsperiode(map: Map<TpOrdningFullDto, List<OpptjeningsperiodeDto>>) =
            map.flatMap { (key, list) ->
                list.map { value ->
                    key to value
                }
            }.ifEmpty { throw MissingOpptjeningsperiodeException("Could not find any stillingsprosent") }
                    .reduce(::getLatest).first

    @Throws(DuplicateOpptjeningsperiodeEndDateException::class)
    private fun getLatest(latest: Pair<TpOrdningFullDto, OpptjeningsperiodeDto>, other: Pair<TpOrdningFullDto, OpptjeningsperiodeDto>) = when {
        latest.second.datoTom?.equals(other.second.datoTom) == true -> throw DuplicateOpptjeningsperiodeEndDateException("Could not decide latest stillingprosent due to multiple stillingsprosent having the same end date")
        other.second.datoTom == null -> other
        latest.second.datoTom == null || latest.second.datoTom!! > other.second.datoTom -> latest
        latest.second.datoTom!! < other.second.datoTom -> other
        else -> latest
    }

    fun mapStillingsprosentToOpptjeningsperiodeList(stillingsprosentList: List<Stillingsprosent>): List<OpptjeningsperiodeDto> {
        return stillingsprosentList.map {
            OpptjeningsperiodeDto(
                    datoFom = it.datoFom,
                    datoTom = it.datoTom,
                    stillingsprosent = it.stillingsprosent,
                    aldersgrense = it.aldersgrense,
                    faktiskHovedlonn = it.faktiskHovedlonn?.toIntOrNull(),
                    stillingsuavhengigTilleggslonn = it.stillingsuavhengigTilleggslonn?.toIntOrNull()
            )
        }
    }
}