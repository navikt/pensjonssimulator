package no.nav.pensjon.simulator.tpregisteret

import java.time.LocalDate

data class TpOrdning(
    val navn: String,
    val tpNr: String,
    val datoSistOpptjening: LocalDate? = null,
    val tssId: String
)
