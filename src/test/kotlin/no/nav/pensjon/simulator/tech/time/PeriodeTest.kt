package no.nav.pensjon.simulator.tech.time

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PeriodeTest: FunSpec( {

    test("fitsIn true") {
        Periode.of(LocalDate.of(2021, 1, 2), LocalDate.of(2021, 1, 30))
            .fitsIn(Periode.of(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31))) shouldBe true
    }

    test("fitsIn false") {
        Periode.of(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 1))
            .fitsIn(Periode.of(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31))) shouldBe false
    }

    test("sameStart true") {
        Periode.of(LocalDate.of(2022, 2, 2), LocalDate.of(2023, 1, 1))
            .sameStart(Periode.of(LocalDate.of(2022, 2, 2), LocalDate.of(2024, 2, 2))) shouldBe true
    }

    test("sameStart false") {
        Periode.of(LocalDate.of(2022, 2, 3), LocalDate.of(2023, 1, 1))
            .sameStart(Periode.of(LocalDate.of(2022, 2, 2), LocalDate.of(2023, 1, 1))) shouldBe false
    }

    test("sameEnd true") {
        Periode.of(LocalDate.of(2021, 1, 1), LocalDate.of(2023, 1, 1))
            .sameEnd(Periode.of(LocalDate.of(2022, 2, 2), LocalDate.of(2023, 1, 1))) shouldBe true
    }

    test("sameEnd false") {
        Periode.of(LocalDate.of(2022, 2, 2), LocalDate.of(2023, 1, 1))
            .sameEnd(Periode.of(LocalDate.of(2022, 2, 2), LocalDate.of(2023, 2, 1))) shouldBe false
    }
})
