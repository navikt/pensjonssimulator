package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import no.nav.pensjon.simulator.validity.Problem
import java.time.LocalDate

data class SimulerOffentligTjenestepensjonResult(
    val tpnr: String,
    val navnOrdning: String,
    val inkluderteOrdningerListe: List<String> = emptyList(),
    val leverandorUrl: String? = null,
    val utbetalingsperiodeListe: List<Utbetalingsperiode> = emptyList(),
    var brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    var brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
    var feilkode: Feilkode? = null,
    val relevanteTpOrdninger: List<String> = emptyList(),
    val problem: Problem? = null
) {
    data class Utbetalingsperiode(
        var uttaksgrad: Int,
        var arligUtbetaling: Double,
        var datoFom: LocalDate,
        var datoTom: LocalDate?,
        var ytelsekode: YtelseCode?
    )

    enum class YtelseCode {
        AP,
        AFP,
        SERALDER
    }

    companion object {
        fun ikkeMedlem() =
            SimulerOffentligTjenestepensjonResult(
                tpnr = "",
                navnOrdning = "",
                inkluderteOrdningerListe = emptyList(),
                brukerErIkkeMedlemAvTPOrdning = true
            )

        fun tpOrdningStoettesIkke(relevanteTpOrdninger: List<String> = emptyList()) =
            SimulerOffentligTjenestepensjonResult(
                tpnr = "",
                navnOrdning = "",
                inkluderteOrdningerListe = emptyList(),
                brukerErMedlemAvTPOrdningSomIkkeStoettes = true,
                relevanteTpOrdninger = relevanteTpOrdninger
            )
    }
}

enum class Feilkode(
    val externalValues: List<String> = emptyList(),
    val externalErrorMessages: List<String> = emptyList()
) {
    TEKNISK_FEIL(
        externalValues = listOf("CALC001", "CALC003", "CALC004", "CALC005")
    ),
    BEREGNING_GIR_NULL_UTBETALING(
        externalValues = listOf("CALC002"),
        externalErrorMessages = listOf("Beregning gir 0 i utbetaling.").map { "Validation problem: $it" }),
    OPPFYLLER_IKKE_INNGANGSVILKAAR(
        externalValues = listOf("CALC002"),
        externalErrorMessages = listOf(
            "Beregning gir 0 i utbetaling.",
            "Delvis AFP ikke lovlig med reststilling under 60%.",
            "Delvis AFP ikke lovlig med nedgang under 10%.",
            "Tjenestetid mindre enn 3 år."
        ).map { "Validation problem: $it" }),
    BRUKER_IKKE_MEDLEM_AV_TP_ORDNING,
    TP_ORDNING_STOETTES_IKKE;

    companion object {
        fun fromExternalValue(externalValue: String, externalErrorMessage: String) =
            entries.firstOrNull {
                if (externalValue == "CALC002")
                    it.externalValues.contains(externalValue) && it.externalErrorMessages.contains(externalErrorMessage)
                else
                    it.externalValues.contains(externalValue)
            } ?: TEKNISK_FEIL
    }
}