package no.nav.pensjon.simulator.tjenestepensjon

import java.time.LocalDate

data class Tjenestepensjonsordning(
    val navn: String,
    val tpNr: String,
    val datoSistOpptjening: LocalDate? = null,
    val tssId: String
    //TODO: Bruk Tjenestepensjonsforhold istedenfor {navn, tpNr, datoSistOpptjening} ?
)