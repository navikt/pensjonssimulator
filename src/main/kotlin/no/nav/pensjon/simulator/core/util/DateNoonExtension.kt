package no.nav.pensjon.simulator.core.util

import java.util.*

// no.nav.consumer.pensjon.pen.regler.common.mapper.PenReglerPenDateMapper
object DateNoonExtension {
    val norwegianLocale = Locale("nb", "NO")
    val norwegianTimeZone = TimeZone.getTimeZone("Europe/Oslo")

    fun Date.noon(): Date {
        val calendar = Calendar.getInstance(norwegianTimeZone, norwegianLocale)
        calendar.time = this
        calendar[Calendar.HOUR_OF_DAY] = 12
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }
}
