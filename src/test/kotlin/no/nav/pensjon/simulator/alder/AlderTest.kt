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

    test("illustrate difference between 'from' and 'oppnaasDato'") {
        val foedselsdato = LocalDate.of(2000, 1, 1) // anta fødselstidspunkt kl 12 denne dagen
        val aktuellDato = LocalDate.of(2050, 3, 1) // anta tidspunkt kl 00 denne dagen

        Alder.from(foedselsdato, dato = aktuellDato) // = 50 år 1 måned (mangler 12 timer på måned 2)
            .oppnaasDato(foedselsdato) shouldBe LocalDate.of(2050, 2, 1) // = 2000-01-01 + 50 år 1 måned
    }

    test("'greaterThan' should return 'true' if first alder is greater than second alder, 'false' otherwise") {
        Alder(66, 11) greaterThan Alder(67, 0) shouldBe false
        Alder(67, 0) greaterThan Alder(67, 0) shouldBe false
        Alder(67, 1) greaterThan Alder(67, 0) shouldBe true
    }

    test("'lessThan' should return 'true' if first alder is less than second alder, 'false' otherwise") {
        Alder(66, 11) lessThan Alder(67, 0) shouldBe true
        Alder(67, 0) lessThan Alder(67, 0) shouldBe false
        Alder(67, 1) lessThan Alder(67, 0) shouldBe false
    }

    test("'antallMaanederEtter' should return antallet måneder den angitte alderen er etter foreliggende alder") {
        Alder(67, 0).antallMaanederEtter(Alder(67, 0)) shouldBe 0
        Alder(67, 2).antallMaanederEtter(Alder(67, 1)) shouldBe 1
        Alder(67, 1).antallMaanederEtter(Alder(66, 11)) shouldBe 2
        Alder(67, 10).antallMaanederEtter(Alder(67, 11)) shouldBe -1
    }

    test("'minusMaaneder' should subtract the given number of måneder") {
        Alder(66, 11).minusMaaneder(1) shouldBe Alder(66, 10)
        Alder(66, 5).minusMaaneder(6) shouldBe Alder(65, 11)
        Alder(66, 8).minusMaaneder(-6) shouldBe Alder(67, 2)
        Alder(66, 8).minusMaaneder(28) shouldBe Alder(64, 4)
    }

    test("'plusMaaneder' should add the given number of måneder") {
        Alder(66, 11).plusMaaneder(1) shouldBe Alder(67, 0)
        Alder(66, 5).plusMaaneder(6) shouldBe Alder(66, 11)
        Alder(66, 8).plusMaaneder(-6) shouldBe Alder(66, 2)
        Alder(66, 6).plusMaaneder(28) shouldBe Alder(68, 10)
    }

    test("'oppnaasDato' should return datoen da alderen oppnås") {
        Alder(aar = 50, maaneder = 3).oppnaasDato(foedselsdato = LocalDate.of(2000, 6, 15)) shouldBe
                LocalDate.of(2050, 9, 15)

        Alder(aar = 50, maaneder = 2).oppnaasDato(foedselsdato = LocalDate.of(2000, 12, 31)) shouldBe
                LocalDate.of(2051, 2, 28) // ikke skuddår

        Alder(aar = 48, maaneder = 1).oppnaasDato(foedselsdato = LocalDate.of(2000, 1, 30)) shouldBe
                LocalDate.of(2048, 2, 29) // skuddår
    }
})
