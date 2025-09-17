package no.nav.pensjon.simulator.tpregisteret.acl

import java.time.LocalDate

data class TpForhold (
    val tpNr: String,
    val navn: String,
    val datoSistOpptjening: LocalDate?,
)