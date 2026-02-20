package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import org.springframework.stereotype.Service

@Service
class OpptjeningsperiodeService {

    fun getOpptjeningsperiodeListe(tpOrdning: TpOrdning, stillingsprosentListe: List<Stillingsprosent>) =
        OpptjeningsperiodeResponse(
            mapOf(tpOrdning to mapStillingsprosentToOpptjeningsperiodeList(stillingsprosentListe)),
            emptyList()
        )
/*
    fun getLatestFromOpptjeningsperiode(map: Map<TpOrdningFull, List<OpptjeningsperiodeDto>>) =
        map.flatMap { (key, list) ->
            list.map { value ->
                key to value
            }
        }.ifEmpty { throw MissingOpptjeningsperiodeException("Could not find any stillingsprosent") }
            .reduce(::getLatest).first

    private fun getLatest(
        latest: Pair<TpOrdningFull, OpptjeningsperiodeDto>,
        other: Pair<TpOrdningFull, OpptjeningsperiodeDto>
    ) =
        when {
            latest.second.datoTom?.equals(other.second.datoTom) == true -> throw DuplicateOpptjeningsperiodeEndDateException(
                "Could not decide latest stillingprosent due to multiple stillingsprosent having the same end date"
            )

            other.second.datoTom == null -> other
            latest.second.datoTom == null || latest.second.datoTom!! > other.second.datoTom -> latest
            latest.second.datoTom!! < other.second.datoTom -> other
            else -> latest
        }
*/
    private fun mapStillingsprosentToOpptjeningsperiodeList(
        stillingsprosentListe: List<Stillingsprosent>
    ): List<OpptjeningsperiodeDto> =
        stillingsprosentListe.map {
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