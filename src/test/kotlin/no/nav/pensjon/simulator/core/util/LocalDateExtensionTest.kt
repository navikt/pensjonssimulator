package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LocalDateExtensionTest : FunSpec({

    test("isBeforeOrOn should be true if not after") {
        LocalDate.of(2023, 12, 31).isBeforeOrOn(LocalDate.of(2024, 1, 1)).shouldBeTrue()
        LocalDate.of(2024, 1, 1).isBeforeOrOn(LocalDate.of(2024, 1, 1)).shouldBeTrue()
    }

    test("isBeforeOrOn should be false if after") {
        LocalDate.of(2024, 1, 2).isBeforeOrOn(LocalDate.of(2024, 1, 1)).shouldBeFalse()
    }
})
