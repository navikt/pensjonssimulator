package no.nav.pensjon.simulator.tjenestepensjon

import java.time.LocalDate

data class Tjenestepensjonsforhold (
    val tpNr: String,
    val navn: String,
    val datoSistOpptjening: LocalDate?
)