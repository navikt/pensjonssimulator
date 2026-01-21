package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode

object SimulerOffentligTjenestepensjonResultMapperV3 {

    fun toDto(result: SimulerOffentligTjenestepensjonResult): SimulerOffentligTjenestepensjonResultV3 {

        if (result.brukerErIkkeMedlemAvTPOrdning) {
            return SimulerOffentligTjenestepensjonResultV3(emptyList(), Feilkode.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING)
        }

        if (result.brukerErMedlemAvTPOrdningSomIkkeStoettes) {
            return SimulerOffentligTjenestepensjonResultV3(
                simulertPensjonListe = null,
                feilkode = Feilkode.TP_ORDNING_STOETTES_IKKE,
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
            feilkode = result.feilkode?.let { Feilkode.valueOf(it.name) },
            relevanteTpOrdninger = result.relevanteTpOrdninger
        )
    }
}