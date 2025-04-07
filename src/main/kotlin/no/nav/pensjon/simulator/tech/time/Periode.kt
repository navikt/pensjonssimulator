package no.nav.pensjon.simulator.tech.time

import java.time.LocalDate

// PEN: no.nav.domain.pensjon.common.util.Periode
data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
) {
    fun fitsIn(other: Periode): Boolean =
        !fom.isBefore(other.fom) && !tom.isAfter(other.tom)

    fun sameStart(other: Periode): Boolean =
        fom == other.fom

    fun sameEnd(other: Periode): Boolean =
        tom == other.tom

    companion object {
        fun of(fom: LocalDate?, tom: LocalDate?) =
            Periode(fom ?: LocalDate.MIN, tom ?: LocalDate.MAX)
    }
}
