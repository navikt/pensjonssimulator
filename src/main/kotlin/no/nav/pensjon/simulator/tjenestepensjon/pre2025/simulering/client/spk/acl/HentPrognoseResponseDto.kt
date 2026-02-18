package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl

import java.time.LocalDate

data class HentPrognoseResponseDto(
    val tpnr: String,
    val navnOrdning: String,
    val inkluderteOrdningerListe: List<String> = emptyList(),
    val leverandorUrl: String? = null,
    val utbetalingsperiodeListe: List<UtbetalingsperiodeDto?> = emptyList(),
    var brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    var brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
)

data class UtbetalingsperiodeDto(
    val uttaksgrad: Int,
    val arligUtbetaling: Double,
    val datoFom: LocalDate,
    val datoTom: LocalDate?,
    val ytelsekode: String
)