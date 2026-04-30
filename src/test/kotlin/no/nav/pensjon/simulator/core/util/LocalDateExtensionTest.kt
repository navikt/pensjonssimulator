package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import java.time.LocalDate
import java.util.*

class LocalDateExtensionTest : ShouldSpec({

    context("isBeforeLd") {
        should("give 'true' if date is before other date") {
            january1.isBeforeLd(january2) shouldBe true
        }

        should("give 'false' if date is same as other date") {
            january1.isBeforeLd(january1) shouldBe false
        }

        should("give 'false' if date is after other date") {
            january2.isBeforeLd(january1) shouldBe false
        }

        /**
         * NB: DateUtil.isBeforeByDay treats null as 1 January year zero.
         */
        should("give 'false' if other date is undefined") {
            january1.isBeforeLd(null) shouldBe false
            DateUtil.isBeforeByDay(january1, null as Date?, allowSameDay = false) shouldBe false
        }
    }

    context("isAfterLd") {
        should("give 'false' if date is before other date") {
            january1.isAfterLd(january2) shouldBe false
        }

        should("give 'false' if date is same as other date") {
            january1.isAfterLd(january1) shouldBe false
        }

        should("give 'true' if date is after other date") {
            january2.isAfterLd(january1) shouldBe true
        }

        should("give 'true' if other date is undefined") {
            january2.isAfterLd(null) shouldBe true
            DateUtil.isAfterByDay(january2, null as Date?, allowSameDay = false) shouldBe true
        }
    }

    context("isBeforeOrOn") {
        should("be true if not after") {
            LocalDate.of(2023, 12, 31).isBeforeOrOn(LocalDate.of(2024, 1, 1)).shouldBeTrue()
            LocalDate.of(2024, 1, 1).isBeforeOrOn(LocalDate.of(2024, 1, 1)).shouldBeTrue()
        }

        should("be false if after") {
            LocalDate.of(2024, 1, 2).isBeforeOrOn(LocalDate.of(2024, 1, 1)).shouldBeFalse()
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

        should("treat nulls as same") {
            DateUtil.isBeforeByDay(null as? Date?, null as? Date?, allowSameDay = true) shouldBe true
            null.isBeforeOrSame(null) shouldBe true
        }

        should("treat null as year zero") {
            DateUtil.isBeforeByDay(null as? Date?, january1, allowSameDay = true) shouldBe true
            null.isBeforeOrSame(january1) shouldBe true
        }

        should("treat other null as year zero") {
            DateUtil.isBeforeByDay(january1, null as? Date?, allowSameDay = true) shouldBe false
            january1.isBeforeOrSame(null) shouldBe false
        }

        should("give 'true' if dates are the same") {
            DateUtil.isBeforeByDay(january1, january1, allowSameDay = true) shouldBe true
            january1.isBeforeOrSame(january1) shouldBe true
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
            DateUtil.isAfterByDay(january2, null as Date?, allowSameDay = true) shouldBe true
        }
    }
})

private val january1 =
    LocalDate.of(2024, 1, 1)

private val january2 =
    LocalDate.of(2024, 1, 2)
