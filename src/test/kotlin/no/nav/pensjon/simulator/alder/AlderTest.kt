package no.nav.pensjon.simulator.alder

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class AlderTest : ShouldSpec({

    context("funksjonen 'from'") {
        should("gi alder som antall hele år og hele måneder etter fødselsdato") {
            Alder.from(
                foedselsdato = LocalDate.of(2001, 2, 3),
                dato = LocalDate.of(2002, 3, 4) // 1 år, 1 måned og 1 dag etter fødselsdato
            ) shouldBe Alder(aar = 1, maaneder = 1)
        }

        should("ikke ta med delvis måned") {
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
        should("anse at ved samme månedsdag anses måneden som ufullstendig") {
            Alder.from(
                foedselsdato = LocalDate.of(2001, 7, 1),
                dato = LocalDate.of(2063, 8, 1) // 62 år, 1 måned, 0 dager, MINUS "noen timer" (kan man forestille seg)
            ) shouldBe Alder(aar = 62, maaneder = 0)
        }

        should("være forskjellig fra 'oppnaasDato'") {
            val foedselsdato = LocalDate.of(2000, 1, 1) // anta fødselstidspunkt kl 12 denne dagen
            val aktuellDato = LocalDate.of(2050, 3, 1) // anta tidspunkt kl 00 denne dagen

            Alder.from(foedselsdato, dato = aktuellDato) // = 50 år 1 måned (mangler 12 timer på måned 2)
                .oppnaasDato(foedselsdato) shouldBe LocalDate.of(2050, 2, 1) // = 2000-01-01 + 50 år 1 måned
        }
    }

    context("funksjonen 'likEllerHoeyereEnn'") {
        context("lavere enn") {
            should("gi 'false'") {
                Alder(aar = 66, maaneder = 11) likEllerHoeyereEnn Alder(aar = 67, maaneder = 0) shouldBe false
            }
        }

        context("lik") {
            should("gi 'true'") {
                Alder(aar = 66, maaneder = 11) likEllerHoeyereEnn Alder(aar = 66, maaneder = 11) shouldBe true
            }
        }

        context("høyere enn") {
            should("gi 'true'") {
                Alder(aar = 67, maaneder = 0) likEllerHoeyereEnn Alder(aar = 66, maaneder = 11) shouldBe true
                Alder(aar = 67, maaneder = 0) likEllerHoeyereEnn null shouldBe true
            }
        }
    }

    context("funksjonen 'greaterThan'") {
        context("mindre enn eller lik") {
            should("gi 'false'") {
                Alder(aar = 66, maaneder = 11) greaterThan Alder(aar = 67, maaneder = 0) shouldBe false
                Alder(aar = 67, maaneder = 0) greaterThan Alder(aar = 67, maaneder = 0) shouldBe false
            }
        }

        context("større enn") {
            should("gi 'true'") {
                Alder(aar = 67, maaneder = 1) greaterThan Alder(aar = 67, maaneder = 0) shouldBe true
                Alder(aar = 67, maaneder = 1) greaterThan null shouldBe true
            }
        }
    }

    context("funksjonen 'lessThan'") {
        context("mindre enn") {
            should("gi 'true'") {
                Alder(aar = 66, maaneder = 11) lessThan Alder(aar = 67, maaneder = 0) shouldBe true
                Alder(aar = 67, maaneder = 1) lessThan null shouldBe true // NB
            }
        }

        context("lik eller større enn") {
            should("gi 'false'") {
                Alder(aar = 67, maaneder = 0) lessThan Alder(aar = 67, maaneder = 0) shouldBe false
                Alder(aar = 67, maaneder = 1) lessThan Alder(aar = 67, maaneder = 0) shouldBe false
            }
        }
    }

    context("funksjonen 'antallMaanederEtter'") {
        should("gi antallet måneder den angitte alderen er etter foreliggende alder") {
            Alder(aar = 67, maaneder = 0).antallMaanederEtter(Alder(aar = 67, maaneder = 0)) shouldBe 0
            Alder(aar = 67, maaneder = 2).antallMaanederEtter(Alder(aar = 67, maaneder = 1)) shouldBe 1
            Alder(aar = 67, maaneder = 1).antallMaanederEtter(Alder(aar = 66, maaneder = 11)) shouldBe 2
            Alder(aar = 67, maaneder = 10).antallMaanederEtter(Alder(aar = 67, maaneder = 11)) shouldBe -1
        }
    }

    context("funksjonen 'minusMaaneder'") {
        should("trekke fra antall måneder") {
            Alder(aar = 66, maaneder = 11).minusMaaneder(1) shouldBe Alder(aar = 66, maaneder = 10)
            Alder(aar = 66, maaneder = 5).minusMaaneder(6) shouldBe Alder(aar = 65, maaneder = 11)
            Alder(aar = 66, maaneder = 8).minusMaaneder(-6) shouldBe Alder(aar = 67, maaneder = 2)
            Alder(aar = 66, maaneder = 8).minusMaaneder(28) shouldBe Alder(aar = 64, maaneder = 4)
        }
    }

    context("funksjonen 'plusMaaneder'") {
        should("legge til antall måneder") {
            Alder(aar = 66, maaneder = 11).plusMaaneder(1) shouldBe Alder(aar = 67, maaneder = 0)
            Alder(aar = 66, maaneder = 5).plusMaaneder(6) shouldBe Alder(aar = 66, maaneder = 11)
            Alder(aar = 66, maaneder = 8).plusMaaneder(-6) shouldBe Alder(aar = 66, maaneder = 2)
            Alder(aar = 66, maaneder = 6).plusMaaneder(28) shouldBe Alder(aar = 68, maaneder = 10)
        }
    }

    context("funksjonen 'oppnaasDato'") {
        should("gi datoen da alderen oppnås") {
            Alder(aar = 50, maaneder = 3).oppnaasDato(foedselsdato = LocalDate.of(2000, 6, 15)) shouldBe
                    LocalDate.of(2050, 9, 15)

            Alder(aar = 50, maaneder = 2).oppnaasDato(foedselsdato = LocalDate.of(2000, 12, 31)) shouldBe
                    LocalDate.of(2051, 2, 28) // ikke skuddår

            Alder(aar = 48, maaneder = 1).oppnaasDato(foedselsdato = LocalDate.of(2000, 1, 30)) shouldBe
                    LocalDate.of(2048, 2, 29) // skuddår
        }
    }
})