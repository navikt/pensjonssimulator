package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import no.nav.pensjon.simulator.validity.Problem
import java.time.LocalDate

data class SimulerOffentligTjenestepensjonResult(
    val tpnr: String,
    val navnOrdning: String,
    val inkluderteOrdningerListe: List<String> = emptyList(),
    val leverandorUrl: String? = null,
    val utbetalingsperiodeListe: List<Utbetalingsperiode> = emptyList(),
    val brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    val brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
    val feilkode: Feilkode? = null,
    val relevanteTpOrdninger: List<String> = emptyList(),
    val problem: Problem? = null
) {
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

        fun problemresultat(problem: Problem) =
            SimulerOffentligTjenestepensjonResult(tpnr = "", navnOrdning = "", problem = problem)
    }
}

data class Utbetalingsperiode(
    val uttaksgrad: Int,
    val arligUtbetaling: Double,
    val datoFom: LocalDate,
    val datoTom: LocalDate?,
    val ytelsekode: YtelseCode?
)

enum class YtelseCode {
    AP,
    AFP,
    SERALDER
}

enum class Feilkode {
    TEKNISK_FEIL,
    BEREGNING_GIR_NULL_UTBETALING,
    OPPFYLLER_IKKE_INNGANGSVILKAAR,
    BRUKER_IKKE_MEDLEM_AV_TP_ORDNING,
    TP_ORDNING_STOETTES_IKKE
}