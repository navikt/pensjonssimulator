package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode

object SimulerOffentligTjenestepensjonResultMapperV1 {

    fun toDto(source: SimulerOffentligTjenestepensjonResult) =
        SimulerOffentligTjenestepensjonResultV1(
            tpnr = source.tpnr,
            navnOrdning = source.navnOrdning,
            inkluderteOrdningerListe = source.inkluderteOrdningerListe,
            leverandorUrl = source.leverandorUrl,
            utbetalingsperiodeListe = source.utbetalingsperiodeListe.map(::utbetalingsperiode),
            brukerErIkkeMedlemAvTPOrdning = source.brukerErIkkeMedlemAvTPOrdning,
            brukerErMedlemAvTPOrdningSomIkkeStoettes = source.brukerErMedlemAvTPOrdningSomIkkeStoettes,
            feilkode = source.feilkode?.let { Feilkode.valueOf(it.name) },
            relevanteTpOrdninger = source.relevanteTpOrdninger
        )

    private fun utbetalingsperiode(source: Utbetalingsperiode) =
        SimulerOffentligTjenestepensjonResultV1.UtbetalingsperiodeV1(
            uttaksgrad = source.uttaksgrad,
            arligUtbetaling = source.arligUtbetaling,
            datoFom = source.datoFom,
            datoTom = source.datoTom,
            ytelsekode = source.ytelsekode?.let { SimulerOffentligTjenestepensjonResultV1.YtelseCode.valueOf(it.name) }
        )
}
