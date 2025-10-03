package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl

import java.time.LocalDate

data class KlpSimulerTjenestepensjonResponse(
    val inkludertOrdningListe: List<InkludertOrdning>,
    val utbetalingsListe: List<Utbetaling>,
    val arsakIngenUtbetaling: List<ArsakIngenUtbetaling>,
    val betingetTjenestepensjonErInkludert: Boolean,
)

data class InkludertOrdning(
    val tpnr: String
)

data class Utbetaling(
    val fraOgMedDato: LocalDate,
    val manedligUtbetaling: Int,
    val arligUtbetaling: Int,
    val ytelseType: String
)

data class ArsakIngenUtbetaling(
    val statusKode: String,
    val statusBeskrivelse: String,
    val ytelseType: String,
)
