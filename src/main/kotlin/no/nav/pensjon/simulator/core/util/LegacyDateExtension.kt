package no.nav.pensjon.simulator.core.util

import java.time.LocalDate
import java.util.*

fun Date?.toLocalDate(): LocalDate? {
    if (this == null) {
        return null
    }

    val calendar = Calendar.getInstance().also { it.time = this }

    return LocalDate.of(
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH] + 1,
        calendar[Calendar.DAY_OF_MONTH]
    )
}

fun Date.toNorwegianDate() = NorwegianCalendar.forDate(this).time

object NorwegianCalendar {
    private val locale = Locale.of("nb", "NO")
    private val timeZone = TimeZone.getTimeZone("Europe/Oslo")

    fun forDate(date: Date) =
        Calendar.getInstance(timeZone, locale).also {
            it.time = date
            it[Calendar.HOUR_OF_DAY] = 0
            it[Calendar.MINUTE] = 0
            it[Calendar.SECOND] = 0
            it[Calendar.MILLISECOND] = 0
        }
}
