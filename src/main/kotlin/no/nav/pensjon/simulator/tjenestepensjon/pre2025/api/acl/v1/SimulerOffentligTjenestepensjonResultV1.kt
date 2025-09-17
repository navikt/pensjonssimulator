package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import java.time.LocalDate

data class SimulerOffentligTjenestepensjonResultV1(
    val tpnr: String,
    val navnOrdning: String,
    val inkluderteOrdningerListe: List<String> = emptyList(),
    val leverandorUrl: String? = null,
    val utbetalingsperiodeListe: List<UtbetalingsperiodeV1?> = emptyList(),
    var brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    var brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
) {

    data class UtbetalingsperiodeV1(
        var uttaksgrad: Int,
        var arligUtbetaling: Double,
        var datoFom: LocalDate,
        var datoTom: LocalDate?,
        var ytelsekode: String
    )

    companion object {
        fun Companion.ikkeMedlem() = SimulerOffentligTjenestepensjonResultV1("", "", emptyList(), brukerErIkkeMedlemAvTPOrdning = true)
        fun Companion.tpOrdningStoettesIkke() = SimulerOffentligTjenestepensjonResultV1("", "", emptyList(), brukerErMedlemAvTPOrdningSomIkkeStoettes = true)
    }
}