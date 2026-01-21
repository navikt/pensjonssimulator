package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult

object SimulerOffentligTjenestepensjonResultMapperV3 {

    fun toDto(result: SimulerOffentligTjenestepensjonResult): SimulerOffentligTjenestepensjonResultV3 {

        if (result.brukerErIkkeMedlemAvTPOrdning) {
            return SimulerOffentligTjenestepensjonResultV3(emptyList(), FeilkodeV3.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING)
        }

        if (result.brukerErMedlemAvTPOrdningSomIkkeStoettes) {
            return SimulerOffentligTjenestepensjonResultV3(
                simulertPensjonListe = null,
                feilkode = FeilkodeV3.TP_ORDNING_STOETTES_IKKE,
                relevanteTpOrdninger = result.relevanteTpOrdninger
            )
        }

        val simulertPensjon = SimulertPensjonResultV3(
            tpnr = result.tpnr,
            navnOrdning = result.navnOrdning,
            inkluderteOrdninger = result.inkluderteOrdningerListe,
            leverandorUrl = result.leverandorUrl,
            utbetalingsperioder = result.utbetalingsperiodeListe.map {
                UtbetalingsperiodeResultV3(
                    grad = it.uttaksgrad,
                    arligUtbetaling = it.arligUtbetaling,
                    datoFom = it.datoFom.toNorwegianDate(),
                    datoTom = it.datoTom?.toNorwegianDate(),
                    ytelsekode = it.ytelsekode?.toString()
                )
            }
        )

        return SimulerOffentligTjenestepensjonResultV3(
            listOf(simulertPensjon),
            feilkode = feilkode(result),
            relevanteTpOrdninger = result.relevanteTpOrdninger
        )
    }

    private fun feilkode(source: SimulerOffentligTjenestepensjonResult): FeilkodeV3? =
        source.feilkode?.let(FeilkodeV3::externalValue)
            ?: source.problem?.let { FeilkodeV3.IKKE_PROSESSERBAR_ENTITET }
}