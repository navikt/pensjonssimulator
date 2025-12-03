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
    var errorResponse: SimulerOFTPErrorResponseV1? = null
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
data class SimulerOFTPErrorResponseV1(
    val errorCode: Feilkode,
    val errorMessage: String
)

enum class Feilkode(val externalValue: String, val externalErrorMessages: List<String>) {
    KUNNE_IKKE_SIMULERE("CALC005", emptyList()),
    UKJENT_PRODUKT("CALC004", emptyList()),
    MIDLERTIDIG_TEKNISK_FEIL("CALC003", emptyList()),
    BEREGNING_GIR_NULL_UTBETALING("CALC002", listOf("Beregning gir 0 i utbetaling.").map { "Validation problem: $it" }),
    OPPFYLLER_IKKE_INNGANGSVILKAAR("CALC002", listOf(
        "Beregning gir 0 i utbetaling.",
        "Delvis AFP ikke lovlig med reststilling under 60%.",
        "Delvis AFP ikke lovlig med nedgang under 10%.",
        "Tjenestetid mindre enn 3 år.")
        .map { "Validation problem: $it" }),
    ANNEN_FEIL("CALC001", emptyList());

    companion object {
        fun fromExternalValue(externalValue: String, externalErrorMessage: String) = entries.first {
            if (externalValue != "CALC002") {
                it.externalValue == externalValue
            } else {
                it.externalValue == externalValue && it.externalErrorMessages.contains(externalErrorMessage)
            }
        }
    }
}