package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import com.fasterxml.jackson.annotation.JsonFormat
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
        @field:JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd",
            timezone = "CET"
        ) var datoFom: LocalDate,
        @field:JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd",
            timezone = "CET"
        ) var datoTom: LocalDate?,
        var ytelsekode: YtelseCode?
    )

    enum class YtelseCode {
        AP,
        AFP,
        SERALDER
    }


    companion object {
        fun ikkeMedlem() =
            SimulerOffentligTjenestepensjonResultV1("", "", emptyList(), brukerErIkkeMedlemAvTPOrdning = true)

        fun tpOrdningStoettesIkke() = SimulerOffentligTjenestepensjonResultV1(
            "",
            "",
            emptyList(),
            brukerErMedlemAvTPOrdningSomIkkeStoettes = true
        )
    }
}