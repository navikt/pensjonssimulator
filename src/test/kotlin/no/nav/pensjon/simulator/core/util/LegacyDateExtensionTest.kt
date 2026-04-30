package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class LegacyDateExtensionTest : ShouldSpec({

    context("toNorwegianLocalDate") {
        should("gi dato i form av LocalDate") {
            january1.toNorwegianLocalDate() shouldBe LocalDate.of(2024, 1, 1)
        }
    }

    context("toNorwegianDate") {
        should("gi klokkeslett 00:00 med norsk tidssone") {
            // finnishNoon = klokkeslett 11:00 norsk tid
            finnishNoon.time - finnishNoon.toNorwegianDate().time shouldBe 39600000L // 11 hours
        }
    }

    context("toNorwegianNoon") {
        should("gi klokkeslett 12:00 med norsk tidssone") {
            // finnishNoon = klokkeslett 11:00 norsk tid
            finnishNoon.time - finnishNoon.toNorwegianNoon().time shouldBe -3600000L // -1 hour
        }
    }
})

private val finnishNoon: Date =
    Calendar.getInstance(
        TimeZone.getTimeZone("Europe/Helsinki"), Locale.of("fi", "FI")
    ).apply {
        clear()
        this[Calendar.YEAR] = 2026
        this[Calendar.MONTH] = Calendar.JANUARY
        this[Calendar.DAY_OF_MONTH] = 1
        this[Calendar.HOUR_OF_DAY] = 12 // noon
    }.time

private val january1: Date =
    dateAtNoon(2024, Calendar.JANUARY, 1)