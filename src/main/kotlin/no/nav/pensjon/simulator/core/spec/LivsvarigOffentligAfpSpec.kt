package no.nav.pensjon.simulator.core.spec

import java.time.LocalDate

/**
 * Spesifiserer egenskapene til livsvarig AFP i offentlig sektor.
 * Det kan enten dreie seg om en innvilget AFP, eller en framtidig rett til AFP.
 */
data class LivsvarigOffentligAfpSpec(
    val innvilgetAfp: InnvilgetLivsvarigOffentligAfpSpec? = null,
    val rettTilAfpFom: LocalDate? = null
)

/**
 * Spesifiserer egenskapene til en innvilget livsvarig AFP i offentlig sektor.
 */
data class InnvilgetLivsvarigOffentligAfpSpec(
    val aarligBruttoBeloep: Double,
    val uttakFom: LocalDate,
    val sistRegulertGrunnbeloep: Int? = null
)
