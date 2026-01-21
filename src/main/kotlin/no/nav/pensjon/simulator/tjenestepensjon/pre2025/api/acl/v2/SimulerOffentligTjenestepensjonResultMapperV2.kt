package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult

object SimulerOffentligTjenestepensjonResultMapperV2 {

    fun toDto(result: SimulerOffentligTjenestepensjonResult): SimulerOffentligTjenestepensjonResultV2 {

        if (result.brukerErIkkeMedlemAvTPOrdning || result.brukerErMedlemAvTPOrdningSomIkkeStoettes) {
            return SimulerOffentligTjenestepensjonResultV2()
        }

        val simulertPensjon = SimulertPensjonResultV2(
            tpnr = result.tpnr,
            navnOrdning = result.navnOrdning,
            inkluderteOrdninger = result.inkluderteOrdningerListe,
            leverandorUrl = result.leverandorUrl,
            inkluderteTpnr = null,
            utelatteTpnr = null,
            status = null,
            feilkode = null,
            feilbeskrivelse = null,
            utbetalingsperioder = result.utbetalingsperiodeListe.map {
                UtbetalingsperiodeResultV2(
                    grad = it.uttaksgrad,
                    arligUtbetaling = it.arligUtbetaling,
                    datoFom = it.datoFom.toNorwegianDate(),
                    datoTom = it.datoTom?.toNorwegianDate(),
                    ytelsekode = it.ytelsekode?.toString()
                )
            }
        )

        return SimulerOffentligTjenestepensjonResultV2(listOf(simulertPensjon))
    }
}