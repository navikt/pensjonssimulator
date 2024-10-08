package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.util.PeriodeUtil.numberOfMonths
import java.time.LocalDate

class PeriodeUtilTest : FunSpec({

    test("numberOfMonths when input is whole months") {
        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 1),
            end1 = LocalDate.of(2024, 12, 31),

            start2 = LocalDate.of(2024, 1, 1),
            end2 = LocalDate.of(2024, 12, 31)
        ) shouldBe 12

        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 1),
            end1 = LocalDate.of(2024, 12, 31),

            start2 = LocalDate.of(2025, 1, 1),
            end2 = LocalDate.of(2025, 12, 31)
        ) shouldBe 0

        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 1),
            end1 = LocalDate.of(2024, 12, 31),

            start2 = LocalDate.of(2024, 7, 1),
            end2 = LocalDate.of(2024, 12, 31)
        ) shouldBe 6

        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 1),
            end1 = LocalDate.of(2024, 12, 31),

            start2 = LocalDate.of(2024, 1, 1),
            end2 = LocalDate.of(2025, 6, 30)
        ) shouldBe 12
    }

    test("numberOfMonths when input is partial months") {
        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 2), // 1 day short of whole month
            end1 = LocalDate.of(2024, 12, 31),

            start2 = LocalDate.of(2024, 1, 1),
            end2 = LocalDate.of(2024, 12, 31)
        ) shouldBe 11

        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 1),
            end1 = LocalDate.of(2024, 12, 30), // 1 day short of whole month

            start2 = LocalDate.of(2024, 1, 1),
            end2 = LocalDate.of(2024, 12, 30)
        ) shouldBe 11

        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 1),
            end1 = LocalDate.of(2024, 12, 31),

            start2 = LocalDate.of(2024, 12, 31), // 1 overlapping day
            end2 = LocalDate.of(2025, 12, 31)
        ) shouldBe 0

        numberOfMonths(
            start1 = LocalDate.of(2024, 2, 2),
            end1 = LocalDate.of(2024, 3, 31),

            start2 = LocalDate.of(2024, 2, 3),
            end2 = LocalDate.of(2024, 3, 30)
        ) shouldBe 1

        numberOfMonths(
            start1 = LocalDate.of(2024, 1, 15),
            end1 = LocalDate.of(2026, 12, 16),

            start2 = LocalDate.of(2024, 1, 15),
            end2 = LocalDate.of(2027, 1, 1)
        ) shouldBe 35
    }
})
