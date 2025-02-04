package no.nav.pensjon.simulator.alder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class AlderTest : FunSpec({

    test("'from' gir alder som antall hele år og hele måneder etter fødselsdato") {
        Alder.from(
            foedselsdato = LocalDate.of(2001, 2, 3),
            dato = LocalDate.of(2002, 3, 4) // 1 år, 1 måned og 1 dag etter fødselsdato
        ) shouldBe Alder(aar = 1, maaneder = 1)
    }

    test("'from' tar ikke med delvis måned") {
        Alder.from(
            foedselsdato = LocalDate.of(2001, 3, 1),
            dato = LocalDate.of(2002, 3, 31) // 1 år, 1 måned minus 1 dag etter fødselsdato
        ) shouldBe Alder(aar = 1, maaneder = 0)

        Alder.from(
            foedselsdato = LocalDate.of(2001, 1, 2),
            dato = LocalDate.of(2002, 2, 1) // 1 år, 1 måned minus 1 dag etter fødselsdato
        ) shouldBe Alder(aar = 1, maaneder = 0)

        Alder.from(
            foedselsdato = LocalDate.of(1963, 1, 2),
            dato = LocalDate.of(2025, 3, 1)
        ) shouldBe Alder(aar = 62, maaneder = 1)
    }

    /**
     * Denne testen illustrerer at uttaksdato er 1. dag i NESTE måned, selv om personen har fødselsdag den 1. i måneden.
     * Eksempel:
     * Fødselsdato = 2001-07-01
     * Alder ved uttak = 62 år 0 måneder
     * Dermed blir uttaksdato = 2063-08-01 (= 2001-07-01 + 62:0 + 1 måned)
     */
    test("'from' anser at ved samme månedsdag anses måneden som ufullstendig") {
        Alder.from(
            foedselsdato = LocalDate.of(2001, 7, 1),
            dato = LocalDate.of(2063, 8, 1) // 62 år, 1 måned, 0 dager, MINUS "noen timer" (kan man forestille seg)
        ) shouldBe Alder(aar = 62, maaneder = 0)
    }
})
