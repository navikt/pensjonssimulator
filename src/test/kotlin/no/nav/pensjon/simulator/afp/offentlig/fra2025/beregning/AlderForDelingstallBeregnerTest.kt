package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

class AlderForDelingstallBeregnerTest : ShouldSpec({

    should("bestemme alder ved uttaksalder 62 år") {
        val alderListe = AlderForDelingstallBeregner.bestemAldreForDelingstall(
            fodselsdato = LocalDate.of(1963, 2, 3),
            uttaksdato = LocalDate.of(2025, 4, 1)
        )

        alderListe shouldHaveSize 2
        with(alderListe[0].alder) {
            aar shouldBe 62
            maaneder shouldBe 1
        }
        with(alderListe[1].alder) {
            aar shouldBe 62
            maaneder shouldBe 10
        }
    }

    should("bestemme alder ved uttaksalder over 70 år er upåvirket av høyeste alder for delingstall") {
        val alderListe = AlderForDelingstallBeregner.bestemAldreForDelingstall(
            fodselsdato = LocalDate.of(1963, 12, 24),
            uttaksdato = LocalDate.of(2036, 4, 1)
        )

        alderListe shouldHaveSize 1
        alderListe[0].alder shouldBe Alder(72, 3)
    }

    should("bestemme alder ved uttaksalder mellom 62 og 70 år") {
        val alderListe = AlderForDelingstallBeregner.bestemAldreForDelingstall(
            fodselsdato = LocalDate.of(1964, 4, 15),
            uttaksdato = LocalDate.of(2029, 1, 1)
        )

        alderListe shouldHaveSize 1
        with(alderListe[0].alder) {
            aar shouldBe 64
            maaneder shouldBe 8
        }
    }

    should("bestemme alder ved uttaksalder 67 år 0 måneder") {
        val alderListe = AlderForDelingstallBeregner.bestemAldreForDelingstall(
            fodselsdato = LocalDate.of(2001, 1, 1),
            uttaksdato = LocalDate.of(2068, 2, 1)
        )

        alderListe shouldHaveSize 1
        with(alderListe[0].alder) {
            aar shouldBe 67
            maaneder shouldBe 0
        }
    }

    should("bestemme alder ved uttaksalder 64 år 8 måneder") {
        val alderListe = AlderForDelingstallBeregner.bestemAldreForDelingstall(
            fodselsdato = LocalDate.of(2001, 4, 1),
            uttaksdato = LocalDate.of(2066, 1, 1)
        )

        alderListe shouldHaveSize 1
        with(alderListe[0].alder) {
            aar shouldBe 64
            maaneder shouldBe 8
        }
    }

    should("bestemme alder ved uttaksalder 64 år 9 måneder") {
        val alderListe = AlderForDelingstallBeregner.bestemAldreForDelingstall(
            fodselsdato = LocalDate.of(2001, 4, 1),
            uttaksdato = LocalDate.of(2066, 1, 15)
        )

        alderListe shouldHaveSize 1
        with(alderListe[0].alder) {
            aar shouldBe 64
            maaneder shouldBe 9
        }
    }
})