package no.nav.pensjon.simulator.core.util

import no.nav.pensjon.simulator.core.util.LocalDateUtil.norwegianDate
import no.nav.pensjon.simulator.core.util.LocalDateUtil.norwegianDateAtNoon
import java.time.LocalDate
import java.util.Date

// no.nav.domain.pensjon.common.util.LocalDateEx

fun LocalDate.isOnOrAfter(other: LocalDate): Boolean {
    return this.isAfter(other) || this.isEqual(other)
}

fun LocalDate.isBeforeOrOn(other: LocalDate): Boolean {
    return this.isBefore(other) || this.isEqual(other)
}

fun LocalDate.toNorwegianDate(): Date = norwegianDate(this)
fun LocalDate.toNorwegianDateAtNoon(): Date = norwegianDateAtNoon(this)
