package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class SPKSimulerTjenestepensjonResponse(
    val inkludertOrdningListe: List<InkludertOrdning>,
    val utbetalingListe: List<Utbetaling>,
    val aarsakIngenUtbetaling: List<AarsakIngenUtbetaling>,
)

data class InkludertOrdning(
    val tpnr: String
)

data class Utbetaling(
    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fraOgMedDato: LocalDate,
    val delytelseListe: List<Delytelse>,
)

data class Delytelse(
    val ytelseType: String,
    val maanedligBelop: Int,
)

data class AarsakIngenUtbetaling(
    val statusKode: String,
    val statusBeskrivelse: String,
    val ytelseType: String,
)
