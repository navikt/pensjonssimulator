package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode as InternalFeilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode as InternalYtelseCode

data class SimulerOffentligTjenestepensjonResultV1(
    val tpnr: String,
    val navnOrdning: String,
    val inkluderteOrdningerListe: List<String> = emptyList(),
    val leverandorUrl: String? = null,
    val utbetalingsperiodeListe: List<UtbetalingsperiodeV1?> = emptyList(),
    var brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    var brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
    var feilkode: Feilkode? = null,
    val relevanteTpOrdninger: List<String>? = null
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

    enum class YtelseCode(private val internalValue: InternalYtelseCode) {
        AP(internalValue = InternalYtelseCode.AP),
        AFP(internalValue = InternalYtelseCode.AFP),
        SERALDER(internalValue = InternalYtelseCode.SERALDER);

        companion object {
            fun externalValue(value: InternalYtelseCode): YtelseCode =
                YtelseCode.entries.single { it.internalValue == value }
        }
    }
}

enum class Feilkode(private val internalValue: InternalFeilkode) {
    TEKNISK_FEIL(internalValue = InternalFeilkode.TEKNISK_FEIL),
    BEREGNING_GIR_NULL_UTBETALING(internalValue = InternalFeilkode.BEREGNING_GIR_NULL_UTBETALING),
    OPPFYLLER_IKKE_INNGANGSVILKAAR(internalValue = InternalFeilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR),
    BRUKER_IKKE_MEDLEM_AV_TP_ORDNING(internalValue = InternalFeilkode.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING),
    TP_ORDNING_STOETTES_IKKE(internalValue = InternalFeilkode.TP_ORDNING_STOETTES_IKKE);

    companion object {
        fun externalValue(value: InternalFeilkode): Feilkode =
            Feilkode.entries.singleOrNull { it.internalValue == value } ?: TEKNISK_FEIL
    }
}