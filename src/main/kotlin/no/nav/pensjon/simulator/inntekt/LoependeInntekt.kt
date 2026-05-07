package no.nav.pensjon.simulator.inntekt

import java.time.LocalDate

//TODO Consolidate with FremtidigInntekt
/**
 * Inntekt som gjelder fra og med en angitt dato.
 */
data class LoependeInntekt(
    val aarligBeloep: Int,
    val fom: LocalDate
)