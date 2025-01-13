package no.nav.pensjon.simulator.core.util

import no.nav.pensjon.simulator.core.util.NorwegianCalendar.locale
import no.nav.pensjon.simulator.core.util.NorwegianCalendar.timeZone
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

object LocalDateUtil {
    fun norwegianDate(source: LocalDate): Date =
        norwegianDate(source, hourOfDay = 0)

    fun norwegianDateAtNoon(source: LocalDate): Date =
        norwegianDate(source, hourOfDay = 12)

    private fun norwegianDate(source: LocalDate, hourOfDay: Int): Date =
        Calendar.getInstance(timeZone, locale).also {
            it[source.year, source.monthValue - 1] = source.dayOfMonth
            it[Calendar.HOUR_OF_DAY] = hourOfDay
            it[Calendar.MINUTE] = 0
            it[Calendar.SECOND] = 0
            it[Calendar.MILLISECOND] = 0
        }.time
}
