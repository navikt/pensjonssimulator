package no.nav.pensjon.simulator.core.util

import java.time.LocalDate
import java.util.*

fun Date.toNorwegianLocalDate(): LocalDate =
    NorwegianCalendar.forDate(this).let {
        LocalDate.of(
            it[Calendar.YEAR],
            it[Calendar.MONTH] + 1,
            it[Calendar.DAY_OF_MONTH]
        )
    }

fun Date.toNorwegianDate() = NorwegianCalendar.forDate(this).time
fun Date.toNorwegianNoon() = NorwegianCalendar.norwegianNoon(this)

object NorwegianCalendar {
    val locale = Locale.of("nb", "NO")
    val timeZone = TimeZone.getTimeZone("Europe/Oslo")

    fun instance(): Calendar =
        Calendar.getInstance(timeZone, locale)

    fun dateAtNoon(year: Int, month: Int, day: Int): Date =
        instance().apply {
            clear()
            this[year, month] = day
            this[Calendar.HOUR_OF_DAY] = 12
        }.time

    fun forNoon(date: Date): Calendar =
        forDate(date, hourOfDay = 12)

    fun forDate(date: Date): Calendar =
        forDate(date, hourOfDay = 0)

    fun norwegianNoon(date: Date): Date =
        forDate(date, hourOfDay = 12).time

    private fun forDate(date: Date, hourOfDay: Int) =
        instance().apply {
            time = date
            this[Calendar.HOUR_OF_DAY] = hourOfDay
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }
}
