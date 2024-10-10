package no.nav.pensjon.simulator.testutil

import java.util.*

/**
 * Date-related utility functions for use in tests.
 */
object TestDateUtil {

    fun dateAtNoon(year: Int, month: Int, day: Int): Date =
        Calendar.getInstance().apply {
            this.clear()
            this[year, month] = day
            this[Calendar.HOUR_OF_DAY] = 12
        }.time
}
