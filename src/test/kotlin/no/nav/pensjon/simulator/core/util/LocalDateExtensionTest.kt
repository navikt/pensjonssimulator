package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import java.time.LocalDate
import java.util.Date

class LocalDateExtensionTest : ShouldSpec({

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
        should("treat nulls as same") {
            DateUtil.isBeforeByDay(null as? Date, null as? Date, allowSameDay = true) shouldBe true
            null.isBeforeOrSame(null) shouldBe true
        }

        should("treat null as year zero") {
            DateUtil.isBeforeByDay(null as? Date, LocalDate.of(2021, 1, 1), allowSameDay = true) shouldBe true
            null.isBeforeOrSame(LocalDate.of(2021, 1, 1)) shouldBe true
        }

        should("treat other null as year zero") {
            DateUtil.isBeforeByDay(LocalDate.of(2021, 1, 1), null as? Date, allowSameDay = true) shouldBe false
            LocalDate.of(2021, 1, 1).isBeforeOrSame(null) shouldBe false
        }

        should("give 'true' if dates are the same") {
            DateUtil.isBeforeByDay(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 1), allowSameDay = true) shouldBe true
            LocalDate.of(2021, 1, 1).isBeforeOrSame(LocalDate.of(2021, 1, 1)) shouldBe true
        }
    }
})
