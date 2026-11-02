package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent

import java.time.LocalDate

data class Stillingsprosent(
    val datoFom: LocalDate,
    val datoTom: LocalDate?,
    val stillingsprosent: Double,
    val aldersgrense: Int,
    val faktiskHovedlonn: String?,
    val stillingsuavhengigTilleggslonn: String?
)