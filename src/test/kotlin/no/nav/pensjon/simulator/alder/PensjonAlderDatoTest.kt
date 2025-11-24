package no.nav.pensjon.simulator.alder

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PensjonAlderDatoTest : ShouldSpec({

    context("getAlder for fødselsdato og alder") {
        should("gi dato 1. i måneden etter fødselsdato + alder") {
            val result = PensjonAlderDato(
                foedselDato = LocalDate.of(2001, 1, 1),
                alder = Alder(aar = 1, maaneder = 0)
            )

            result.alder shouldBe Alder(aar = 1, maaneder = 0)
            result.dato shouldBe LocalDate.of(2002, 2, 1)
        }
    }

    context("getAlder for fødselsdato og dato") {
        should("utlede alder") {
            val result = PensjonAlderDato(
                foedselDato = LocalDate.of(2001, 2, 3),
                dato = LocalDate.of(2002, 3, 4) // 1 år, 1 måned og 1 dag etter fødselsdato
            )

            result.alder shouldBe Alder(aar = 1, maaneder = 1)
            result.dato shouldBe LocalDate.of(2002, 3, 4)
        }

        should("utelukke delvis måned") {
            val result = PensjonAlderDato(
                foedselDato = LocalDate.of(2001, 3, 1),
                dato = LocalDate.of(2002, 3, 31) // 1 år, 1 måned minus 1 dag etter fødselsdato
            )

            result.alder shouldBe Alder(aar = 1, maaneder = 0)
            result.dato shouldBe LocalDate.of(2002, 3, 31)
        }

        should("utelukke delvis måned - jan-feb") {
            val result = PensjonAlderDato(
                foedselDato = LocalDate.of(2001, 1, 2),
                dato = LocalDate.of(2002, 2, 1) // 1 år, 1 måned minus 1 dag etter fødselsdato
            )

            result.alder shouldBe Alder(aar = 1, maaneder = 0)
            result.dato shouldBe LocalDate.of(2002, 2, 1)
        }

        should("utelukke delvis måned - jan-mars") {
            val result = PensjonAlderDato(
                foedselDato = LocalDate.of(1963, 1, 2),
                dato = LocalDate.of(2025, 3, 1) // 1 år, 1 måned minus 1 dag etter fødselsdato
            )

            result.alder shouldBe Alder(aar = 62, maaneder = 1)
            result.dato shouldBe LocalDate.of(2025, 3, 1)
        }
    }

    context("getAlder with repeated invocations") {
        should("have stable result") {
            val foedselsdato = LocalDate.of(2001, 2, 1)

            val result1 = PensjonAlderDato(
                foedselsdato,
                dato = LocalDate.of(2002, 3, 4)
            )

            result1.alder shouldBe Alder(aar = 1, maaneder = 1)
            result1.dato shouldBe LocalDate.of(2002, 3, 4)

            val result2 = PensjonAlderDato(
                foedselsdato,
                alder = result1.alder
            )

            result2.alder shouldBe result1.alder // stable
            result2.dato shouldBe LocalDate.of(2002, 4, 1)

            val result3 = PensjonAlderDato(
                foedselsdato,
                dato = result2.dato
            )

            result3.alder shouldBe result2.alder // stable
            result3.dato shouldBe result2.dato // stable

            val result4 = PensjonAlderDato(
                foedselsdato,
                alder = result3.alder
            )

            result4.alder shouldBe result2.alder // stable
            result4.dato shouldBe result2.dato // stable
        }
    }
})
