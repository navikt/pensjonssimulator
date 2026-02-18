package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode

object PrognoseResultMapper {

    fun fromDto(dto: HentPrognoseResponseDto) =
        SimulerOffentligTjenestepensjonResult(
            tpnr = dto.tpnr,
            navnOrdning = dto.navnOrdning,
            inkluderteOrdningerListe = dto.inkluderteOrdningerListe,
            leverandorUrl = dto.leverandorUrl,
            utbetalingsperiodeListe = dto.utbetalingsperiodeListe.filterNotNull().map(::utbetalingsperiode),
            brukerErIkkeMedlemAvTPOrdning = dto.brukerErIkkeMedlemAvTPOrdning,
            brukerErMedlemAvTPOrdningSomIkkeStoettes = dto.brukerErMedlemAvTPOrdningSomIkkeStoettes
        )

    private fun utbetalingsperiode(dto: UtbetalingsperiodeDto) =
        Utbetalingsperiode(
            uttaksgrad = dto.uttaksgrad,
            arligUtbetaling = dto.arligUtbetaling,
            datoFom = dto.datoFom,
            datoTom = dto.datoTom,
            ytelsekode = YtelseCode.valueOf(dto.ytelsekode)
        )
}