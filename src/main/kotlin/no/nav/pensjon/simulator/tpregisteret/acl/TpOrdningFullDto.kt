package no.nav.pensjon.simulator.tpregisteret.acl

import java.time.LocalDate

data class TpOrdningFullDto(
    val navn: String,
    val tpNr: String,
    val datoSistOpptjening: LocalDate? = null,
    val tssId: String,
)
