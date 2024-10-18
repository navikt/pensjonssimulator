package no.nav.pensjon.simulator.core.util

import java.util.*

// no.nav.consumer.pensjon.pen.regler.common.mapper.PenReglerPenDateMapper
object DateNoonExtension {
    val norwegianLocale = Locale.of("nb", "NO")
    val norwegianTimeZone = TimeZone.getTimeZone("Europe/Oslo")

    fun Date.noon(): Date =
        Calendar.getInstance(norwegianTimeZone, norwegianLocale).also {
            it.time = this
            it[Calendar.HOUR_OF_DAY] = 12
            it[Calendar.MINUTE] = 0
            it[Calendar.SECOND] = 0
            it[Calendar.MILLISECOND] = 0
        }.time
}
