package no.nav.pensjon.simulator.tech.time

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object DateUtil {
    private const val TIME_ZONE_ID = "Europe/Oslo"

    fun toLocalDate(dateTime: ZonedDateTime): LocalDate =
        dateTime.withZoneSameInstant(ZoneId.of(TIME_ZONE_ID)).toLocalDate()

    fun foersteDagNesteMaaned(dato: LocalDate): LocalDate =
        dato
            .plusMonths(1)
            .withDayOfMonth(1)
}
