package no.nav.pensjon.simulator.core.util

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import java.time.LocalDate

// no.nav.domain.pensjon.common.util.LocalDateEx

fun LocalDate.isOnOrAfter(other: LocalDate): Boolean {
    return this.isAfter(other) || this.isEqual(other)
}

fun LocalDate.isBeforeOrOn(other: LocalDate): Boolean {
    return this.isBefore(other) || this.isEqual(other)
}

fun LocalDate.toDate() = fromLocalDate(this)!!
