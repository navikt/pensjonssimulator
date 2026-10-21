package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class StorebrandSimulerTjenestepensjonResponse(
    val inkludertOrdningListe: List<InkludertOrdning>,
    @param:JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val utbetalingListe: List<Utbetaling>,
    val aarsakIngenUtbetaling: List<AarsakIngenUtbetaling>?
)

data class InkludertOrdning(
    val tpnr: String
)

data class Utbetaling(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val delytelseListe: List<Delytelse>
)

data class Delytelse(
    val ytelseType: String, // StorebrandTjenestepensjonYtelseType
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