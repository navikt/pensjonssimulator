package no.nav.pensjon.simulator.fpp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Utenlandsopphold
import java.time.LocalDate

class FppTrygdetidBeregnerTest : ShouldSpec({

    /**
     * Utland: 16.02.1976-18.09.1991 = 15 år 7 md 3 dg
     * Trygdetid: 35 år 3 md 13 dg -> 35 år
     */
    should("legge sammen sammenhengende utenlandsopphold") {
        FppTrygdetidBeregner.trygdetidAntallAar(
            foedselsdato,
            utenlandsoppholdListe = listOf(
                Utenlandsopphold().apply {
                    fomLd = foedselsdato
                    tomLd = LocalDate.of(1980, 1, 5)
                },
                Utenlandsopphold().apply {
                    fomLd = LocalDate.of(1980, 1, 6)
                    tomLd = LocalDate.of(1991, 9, 18)
                }
            ),
            flyktning = false
        ) shouldBe 35
    }

    should("legge sammen usammenhengende utenlandsopphold") {
        FppTrygdetidBeregner.trygdetidAntallAar(
            foedselsdato,
            utenlandsoppholdListe = listOf(
                Utenlandsopphold().apply {
                    fomLd = foedselsdato
                    tomLd = LocalDate.of(1980, 8, 30)
                },
                Utenlandsopphold().apply {
                    fomLd = LocalDate.of(1981, 8, 31)
                    tomLd = LocalDate.of(1992, 9, 18)
                }
            ),
            flyktning = false
        ) shouldBe 35
    }

    /**
     * Utland: 16.02.1976-18.06.1991 = 15 år 4 md 3 dg
     * Trygdetid: 35 år 6 md 6 dager -> 36 år
     */
    context("antall måneder er 6 eller mer") {
        should("runde av antall år oppover") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = foedselsdato
                        tomLd = LocalDate.of(1991, 6, 18)
                    }
                ),
                flyktning = false
            ) shouldBe 36
        }
    }

    /**
     * Utland: 16.02.1976-02.07.1991 = 15 år 4 md 17 dg
     * Trygdetid: 35 år 5 md 29 dg -> 35 år
     */
    context("antall måneder er mindre enn 6") {
        should("runde av antall år nedover") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = foedselsdato
                        tomLd = LocalDate.of(1991, 7, 2)
                    }
                ),
                flyktning = false
            ) shouldBe 35
        }
    }
})

/**
 * 1 måned og 15 dager etter årets start.
 */
private val foedselsdato = LocalDate.of(1976, 2, 16)
