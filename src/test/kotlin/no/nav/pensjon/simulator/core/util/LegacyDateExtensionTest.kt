package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
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

    context("isBefore") {
        should("give 'true' if date is before other date") {
            january1.isBefore(january2) shouldBe true
        }

        should("give 'false' if date is same as other date") {
            january1.isBefore(january1) shouldBe false
        }

        should("give 'false' if date is after other date") {
            january2.isBefore(january1) shouldBe false
        }

        /**
         * NB: DateUtil.isBeforeByDay treats null as 1 January year zero.
         */
        should("give 'false' if other date is undefined") {
            january1.isBefore(null) shouldBe false
            DateUtil.isBeforeByDay(january1, null as Date?, allowSameDay = false) shouldBe false
        }
    }

    context("isBeforeOrSame") {
        should("give 'true' if date is before other date") {
            january1.isBeforeOrSame(january2) shouldBe true
        }

        should("give 'true' if date is same as other date") {
            january1.isBeforeOrSame(january1) shouldBe true
        }

        should("give 'false' if date is after other date") {
            january2.isBeforeOrSame(january1) shouldBe false
        }

        should("give 'false' if other date is undefined") {
            january1.isBeforeOrSame(null) shouldBe false
            DateUtil.isBeforeByDay(january1, null as Date?, allowSameDay = true) shouldBe false
        }
    }

    context("isAfterOrSame") {
        should("give 'false' if date is before other date") {
            january1.isAfterOrSame(january2) shouldBe false
        }

        should("give 'true' if date is same as other date") {
            january1.isAfterOrSame(january1) shouldBe true
        }

        should("give 'true' if date is after other date") {
            january2.isAfterOrSame(january1) shouldBe true
        }

        should("give 'true' if other date is undefined") {
            january2.isAfterOrSame(null) shouldBe true
            DateUtil.isAfterByDay(january2, null, allowSameDay = true) shouldBe true
        }
    }

    context("isAfter") {
        should("give 'false' if date is before other date") {
            january1.isAfter(january2) shouldBe false
        }

        should("give 'false' if date is same as other date") {
            january1.isAfter(january1) shouldBe false
        }

        should("give 'true' if date is after other date") {
            january2.isAfter(january1) shouldBe true
        }

        should("give 'true' if other date is undefined") {
            january2.isAfter(null) shouldBe true
            DateUtil.isAfterByDay(january2, null, allowSameDay = false) shouldBe true
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

private val january2: Date =
    dateAtNoon(2024, Calendar.JANUARY, 2)
