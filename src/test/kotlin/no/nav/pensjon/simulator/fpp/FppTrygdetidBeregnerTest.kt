package no.nav.pensjon.simulator.fpp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Utenlandsopphold
import java.time.LocalDate

class FppTrygdetidBeregnerTest : ShouldSpec({

    context("trygdetidAntallAar") {
        /**
         * Utland: 16.02.1976-18.09.1991 = 15 år 7 md 3 dg
         * Trygdetid: 35 år 3 md 13 dg -> 35 år
         */
        should("legge sammen sammenhengende utenlandsopphold") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = LocalDate.of(1976, 2, 16)
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
                        fomLd = LocalDate.of(1976, 2, 16)
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
                            fomLd = LocalDate.of(1976, 2, 16)
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
                            fomLd = LocalDate.of(1976, 2, 16)
                            tomLd = LocalDate.of(1991, 7, 2)
                        }
                    ),
                    flyktning = false
                ) shouldBe 35
            }
        }

        should("ignorere år før minstealder for trygdetid (16 år)") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = LocalDate.of(1974, 2, 16) // 2 år før minstealder
                        tomLd = LocalDate.of(1991, 7, 2)
                    }
                ),
                flyktning = false
            ) shouldBe 35
        }

        should("ignorere år etter maks-alder for opptjening (66 år)") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = LocalDate.of(2000, 1, 1)
                        tomLd = LocalDate.of(2028, 12, 31) // 2 år etter maks-alder
                    }
                ),
                flyktning = false
            ) shouldBe 24
        }

        should("behandle udefinert sluttår som siste opptjeningsår (66 år)") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = LocalDate.of(1974, 2, 16)
                        tomLd = null // anses å være 2026-12-31
                    }
                ),
                flyktning = false
            ) shouldBe 0
        }

        should("behandle udefinert startår som første år med trygdetid (16 år)") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = null // anses å være 1976-02-16
                        tomLd = LocalDate.of(1991, 7, 2)
                    }
                ),
                flyktning = false
            ) shouldBe 35
        }

        should("gi full trygdetid (40 år) hvis flyktning") {
            FppTrygdetidBeregner.trygdetidAntallAar(
                foedselsdato,
                utenlandsoppholdListe = listOf(
                    Utenlandsopphold().apply {
                        fomLd = LocalDate.of(1976, 2, 16)
                        tomLd = LocalDate.of(1991, 6, 18)
                    }
                ),
                flyktning = true
            ) shouldBe 40
        }
    }

    context("unoeyaktigTrygdetidAntallAar") {
        should("beregne trygdetid ved å trekke antall år utenlands fra maksimal opptjeningstid (51 år)") {
            FppTrygdetidBeregner.omtrentligTrygdetidAntallAar(
                antallArUtland = 20,
                flyktning = false
            ) shouldBe 31
        }

        should("gi full trygdetid (40 år) hvis flyktning") {
            FppTrygdetidBeregner.omtrentligTrygdetidAntallAar(
                antallArUtland = 20,
                flyktning = true
            ) shouldBe 40
        }

        should("begrense oppad til 40 år") {
            FppTrygdetidBeregner.omtrentligTrygdetidAntallAar(
                antallArUtland = 0,
                flyktning = false
            ) shouldBe 40

            FppTrygdetidBeregner.omtrentligTrygdetidAntallAar(
                antallArUtland = 5,
                flyktning = false
            ) shouldBe 40
        }

        should("begrense nedad til 0 år") {
            FppTrygdetidBeregner.omtrentligTrygdetidAntallAar(
                antallArUtland = 100,
                flyktning = false
            ) shouldBe 0
        }
    }
})

/**
 * 1 måned og 15 dager etter årets start.
 */
private val foedselsdato = LocalDate.of(1960, 2, 16)
