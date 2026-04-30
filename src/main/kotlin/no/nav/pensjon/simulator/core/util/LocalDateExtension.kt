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

/**
 * Null-supporting variant of LocalDate.isBefore.
 * Here 'null' represents the end of time, so <any date>.isBeforeLd(null) returns 'false'.
 */
fun LocalDate.isBeforeLd(other: LocalDate?): Boolean =
    other?.let(this::isBefore) == true

/**
 * Null-supporting variant of LocalDate.isAfter.
 * Here 'null' represents the start of time, so <any date>.isAfterLd(null) returns 'true'.
 */
fun LocalDate.isAfterLd(other: LocalDate?): Boolean =
    other?.let(this::isAfter) ?: true

/**
 * Null-supporting variant of isBeforeOrOn.
 * Equivalent to the legacy function isBeforeByDay, where null is treated as year 0.
 */
fun LocalDate?.isBeforeOrSame(other: LocalDate?): Boolean =
    (this ?: defaultDate).isBeforeOrOn(other ?: defaultDate)

fun LocalDate.isAfterOrSame(other: LocalDate?): Boolean =
    other?.let { this.isBefore(it).not() } ?: true

fun LocalDate.toNorwegianDate(): Date = norwegianDate(this)
fun LocalDate.toNorwegianDateAtNoon(): Date = norwegianDateAtNoon(this)

private val defaultDate = LocalDate.of(0, 1, 1)
