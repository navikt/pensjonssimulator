package no.nav.pensjon.simulator.tpregisteret

import java.time.LocalDate

data class TpForhold (
    val tpNr: String,
    val navn: String,
    val datoSistOpptjening: LocalDate?,
)