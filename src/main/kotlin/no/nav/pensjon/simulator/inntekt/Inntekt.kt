package no.nav.pensjon.simulator.inntekt

import java.time.LocalDate

//TODO Consolidate with FremtidigInntekt and krav.Inntekt
data class Inntekt(
    val aarligBeloep: Int,
    val fom: LocalDate
)
