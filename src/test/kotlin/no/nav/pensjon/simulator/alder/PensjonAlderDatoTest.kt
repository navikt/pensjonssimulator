package no.nav.pensjon.simulator.alder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PensjonAlderDatoTest : FunSpec({

    test("getAlder for alder og dato") {
        val result = PensjonAlderDato(
            alder = Alder(aar = 1, maaneder = 0),
            dato = LocalDate.of(2001, 1, 1)
        )

        result.alder shouldBe Alder(aar = 1, maaneder = 0)
        result.dato shouldBe LocalDate.of(2001, 1, 1)
    }

    test("getAlder for fødselsdato og alder gir dato 1. i måneden etter fødselsdato + alder") {
        val result = PensjonAlderDato(
            foedselDato = LocalDate.of(2001, 1, 1),
            alder = Alder(aar = 1, maaneder = 0)
        )

        result.alder shouldBe Alder(aar = 1, maaneder = 0)
        result.dato shouldBe LocalDate.of(2002, 2, 1)
    }

    test("getAlder for fødselsdato og dato") {
        val result = PensjonAlderDato(
            foedselDato = LocalDate.of(2001, 2, 3),
            dato = LocalDate.of(2002, 3, 4) // 1 år, 1 måned og 1 dag etter fødselsdato
        )

        result.alder shouldBe Alder(aar = 1, maaneder = 1)
        result.dato shouldBe LocalDate.of(2002, 3, 4)
    }

    test("getAlder for fødselsdato og dato tar ikke med delvis måned") {
        val result = PensjonAlderDato(
            foedselDato = LocalDate.of(2001, 3, 1),
            dato = LocalDate.of(2002, 3, 31) // 1 år, 1 måned minus 1 dag etter fødselsdato
        )

        result.alder shouldBe Alder(aar = 1, maaneder = 0)
        result.dato shouldBe LocalDate.of(2002, 3, 31)
    }
})
