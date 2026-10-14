package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class SpkSimulerTjenestepensjonResponse(
    val inkludertOrdningListe: List<InkludertOrdning>,
    val utbetalingListe: List<Utbetaling>,
    val aarsakIngenUtbetaling: List<AarsakIngenUtbetaling>
)

data class InkludertOrdning(
    val tpnr: String
)

data class Utbetaling(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val delytelseListe: List<Delytelse>
)

data class Delytelse(
    val ytelseType: String, // SpkTjenestepensjonYtelseType
    val maanedligBelop: Int
)

data class AarsakIngenUtbetaling(
    val statusKode: String, // AarsakIngenUtbetalingStatus
    val statusBeskrivelse: String,
    val ytelseType: String
)

enum class AarsakIngenUtbetalingStatus {
    IKKE_SISTE_ORDNING
}