package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult

object SimulerOffentligTjenestepensjonResultMapperV3 {

    fun toDto(source: SimulerOffentligTjenestepensjonResult): SimulerOffentligTjenestepensjonResultV3 {

        if (source.brukerErIkkeMedlemAvTPOrdning) {
            return SimulerOffentligTjenestepensjonResultV3(
                simulertPensjonListe = emptyList(),
                feilkode = FeilkodeV3.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING
            )
        }

        if (source.brukerErMedlemAvTPOrdningSomIkkeStoettes) {
            return SimulerOffentligTjenestepensjonResultV3(
                simulertPensjonListe = null,
                feilkode = FeilkodeV3.TP_ORDNING_STOETTES_IKKE,
                relevanteTpOrdninger = source.relevanteTpOrdninger
            )
        }

        val simulertPensjon = SimulertPensjonResultV3(
            tpnr = source.tpnr,
            navnOrdning = source.navnOrdning,
            inkluderteOrdninger = source.inkluderteOrdningerListe,
            leverandorUrl = source.leverandorUrl,
            utbetalingsperioder = source.utbetalingsperiodeListe.map {
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
            simulertPensjonListe = listOf(simulertPensjon),
            feilkode = feilkodeV3(source),
            relevanteTpOrdninger = source.relevanteTpOrdninger
        )
    }

    private fun feilkodeV3(source: SimulerOffentligTjenestepensjonResult): FeilkodeV3? =
        source.feilkode?.let { FeilkodeV3.valueOf(it.name) }
            ?: source.problem?.let { FeilkodeV3.ANNEN_KLIENTFEIL }
}