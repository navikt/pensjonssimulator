package no.nav.pensjon.simulator.tid

import java.time.LocalDate

/**
 * Representerer en kalendermåned i et gitt år.
 */
data class AarOgMaaned(
    val aar: Int,
    val maaned: Int // 1 t.o.m. 12
) {
    fun erEtter(other: AarOgMaaned): Boolean =
        aar > other.aar || aar == other.aar && maaned > other.maaned

    companion object {
        fun forDato(dato: LocalDate) =
            AarOgMaaned(
                aar = dato.year,
                maaned = dato.monthValue
            )
    }
}