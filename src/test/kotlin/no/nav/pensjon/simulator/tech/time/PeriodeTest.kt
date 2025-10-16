package no.nav.pensjon.simulator.tech.time

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PeriodeTest : ShouldSpec({

    context("fitsIn") {
        should("return true when fitting in") {
            Periode.of(fom = LocalDate.of(2021, 1, 2), tom = LocalDate.of(2021, 1, 30))
                .fitsIn(Periode.of(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2021, 1, 31))) shouldBe true
        }

        should("return false when not fitting in") {
            Periode.of(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2021, 2, 1))
                .fitsIn(Periode.of(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2021, 1, 31))) shouldBe false
        }
    }

    context("sameStart") {
        should("return true when same start") {
            Periode.of(fom = LocalDate.of(2022, 2, 2), tom = LocalDate.of(2023, 1, 1))
                .sameStart(Periode.of(fom = LocalDate.of(2022, 2, 2), tom = LocalDate.of(2024, 2, 2))) shouldBe true
        }

        should("return false when not same start") {
            Periode.of(fom = LocalDate.of(2022, 2, 3), tom = LocalDate.of(2023, 1, 1))
                .sameStart(Periode.of(fom = LocalDate.of(2022, 2, 2), tom = LocalDate.of(2023, 1, 1))) shouldBe false
        }
    }

    context("sameEnd") {
        should("return true when same end") {
            Periode.of(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2023, 1, 1))
                .sameEnd(Periode.of(fom = LocalDate.of(2022, 2, 2), tom = LocalDate.of(2023, 1, 1))) shouldBe true
        }

        should("return false when not same end") {
            Periode.of(fom = LocalDate.of(2022, 2, 2), tom = LocalDate.of(2023, 1, 1))
                .sameEnd(Periode.of(fom = LocalDate.of(2022, 2, 2), tom = LocalDate.of(2023, 2, 1))) shouldBe false
        }
    }
})
