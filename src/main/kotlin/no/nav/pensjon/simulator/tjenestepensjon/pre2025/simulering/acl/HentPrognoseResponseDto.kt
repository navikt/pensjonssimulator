package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl

import java.time.LocalDate

data class HentPrognoseResponseDto(
    val tpnr: String,
    val navnOrdning: String,
    val inkluderteOrdningerListe: List<String> = emptyList(),
    val leverandorUrl: String? = null,
    val utbetalingsperiodeListe: List<Utbetalingsperiode?> = emptyList(),
    var brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    var brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
) {

    companion object {
        fun Companion.ikkeMedlem() = HentPrognoseResponseDto("", "", emptyList(), brukerErIkkeMedlemAvTPOrdning = true)
        fun Companion.tpOrdningStoettesIkke() = HentPrognoseResponseDto("", "", emptyList(), brukerErMedlemAvTPOrdningSomIkkeStoettes = true)
    }
}

data class Utbetalingsperiode(
    val uttaksgrad: Int,
    val arligUtbetaling: Double,
    val datoFom: LocalDate,
    val datoTom: LocalDate?,
    val ytelsekode: String
)
