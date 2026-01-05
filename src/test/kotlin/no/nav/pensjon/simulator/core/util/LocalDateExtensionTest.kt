package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.LocalDate

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
})
