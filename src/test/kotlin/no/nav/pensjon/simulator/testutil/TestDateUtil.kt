package no.nav.pensjon.simulator.testutil

import java.util.*

/**
 * Date-related utility functions for use in tests.
 */
object TestDateUtil {

    fun dateAtMidnight(year: Int, zeroBasedMonth: Int, day: Int): Date =
        dateAt(year, zeroBasedMonth, day, 0)

    fun dateAtNoon(year: Int, zeroBasedMonth: Int, day: Int): Date =
        dateAt(year, zeroBasedMonth, day, 12)

    private fun dateAt(year: Int, zeroBasedMonth: Int, day: Int, hourOfDay: Int): Date =
        Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"),  Locale.of("nb", "NO")).apply {
            this.clear()
            this[year, zeroBasedMonth] = day
            this[Calendar.HOUR_OF_DAY] = hourOfDay
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }.time
}
