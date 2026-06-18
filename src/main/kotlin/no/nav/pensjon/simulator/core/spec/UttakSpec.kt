package no.nav.pensjon.simulator.core.spec

import java.time.LocalDate

/**
 * Spesifikasjon av uttak (uttaksgrad og startdatoer).
 */
data class UttakSpec(
    val uttaksgrad: Int,
    val gradert: Boolean,
    val foersteUttakFom: LocalDate,
    val andreUttakFom: LocalDate?
)