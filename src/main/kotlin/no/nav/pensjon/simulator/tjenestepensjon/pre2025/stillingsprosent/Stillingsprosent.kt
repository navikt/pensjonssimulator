package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent

import java.time.LocalDate

data class Stillingsprosent(
    var datoFom: LocalDate,
    var datoTom: LocalDate?,
    var stillingsprosent: Double,
    var aldersgrense: Int,
    var faktiskHovedlonn: String?,
    var stillingsuavhengigTilleggslonn: String?
)