package no.nav.pensjon.simulator.tech.time

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DateUtilTest : ShouldSpec({

    context("toLocalDate") {
        should("take time zone into account") {
            DateUtil.toLocalDate(
                ZonedDateTime.of(
                    2024, 12, 31, 23, 0, 0, 0,
                    ZoneId.of("UTC") // 1-hour difference compared to Norwegian time zone
                )
            ) shouldBe LocalDate.of(2025, 1, 1)
        }
    }

    context("overlapper") {
        val start1 = LocalDate.of(2021, 3, 1)
        val slutt1 = LocalDate.of(2021, 5, 31)

        should("gi 'true' ved like perioder") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = start1,
                slutt2 = slutt1,
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'false' når hele periode 2 er før periode 1") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = start1.minusMonths(2),
                slutt2 = start1.minusMonths(1),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe false
        }

        should("gi 'false' når hele periode 2 er etter periode 1") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = slutt1.plusMonths(1),
                slutt2 = slutt1.plusMonths(2),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe false
        }

        should("gi 'true' når periode 2 dekker starten av periode 1") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = start1.minusMonths(1),
                slutt2 = start1.plusMonths(1),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'true' når periode 2 dekker slutten av periode 1") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = slutt1.minusMonths(1),
                slutt2 = slutt1.plusMonths(1),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'true' når periode 2 dekker starten og slutten av periode 1") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = start1.minusMonths(1),
                slutt2 = slutt1.plusMonths(1),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'false' når periode 2 er udefinert") {
            DateUtil.overlapper(
                start1,
                slutt1,
                start2 = null,
                slutt2 = null,
                anseEnkeltDagSomOverlapp = false
            ) shouldBe false
        }

        context("når enkeltdag skal anses som overlapp") {
            should("gi 'true' ved 1-dags overlapp ved start") {
                DateUtil.overlapper(
                    start1,
                    slutt1,
                    start2 = start1.minusMonths(1),
                    slutt2 = start1,
                    anseEnkeltDagSomOverlapp = true
                ) shouldBe true
            }

            should("gi 'true' ved 1-dags overlapp ved slutt") {
                DateUtil.overlapper(
                    start1,
                    slutt1,
                    start2 = slutt1,
                    slutt2 = slutt1.plusMonths(1),
                    anseEnkeltDagSomOverlapp = true
                ) shouldBe true
            }

            should("gi 'true' for udefinerte perioder") {
                DateUtil.overlapper(
                    start1 = null,
                    slutt1 = null,
                    start2 = null,
                    slutt2 = null,
                    anseEnkeltDagSomOverlapp = true
                ) shouldBe true
            }
        }

        context("når enkeltdag ikke skal anses som overlapp") {
            should("gi 'false' når periode 2 overlapper starten av periode 1 med en enkelt dag") {
                DateUtil.overlapper(
                    start1,
                    slutt1,
                    start2 = start1.minusMonths(1),
                    slutt2 = start1,
                    anseEnkeltDagSomOverlapp = false
                ) shouldBe false
            }

            should("gi 'false' når periode 2 overlapper slutten av periode 1 med en enkelt dag") {
                DateUtil.overlapper(
                    start1,
                    slutt1,
                    start2 = slutt1,
                    slutt2 = slutt1.plusMonths(1),
                    anseEnkeltDagSomOverlapp = false
                ) shouldBe false
            }

            /**
             * NB: Ved 1-dags periode ignoreres flagget 'anse enkelt dag som overlapp'.
             */
            should("gi 'true' når periode 2 er en enkelt dag som sammenfaller med start av periode 1") {
                DateUtil.overlapper(
                    start1,
                    slutt1,
                    start2 = start1, // 1-dags...
                    slutt2 = start1, // ...periode
                    anseEnkeltDagSomOverlapp = false // ignoreres
                ) shouldBe true
            }

            /**
             * Udefinerte perioder anses å være samme enkeltdag (flagget 'anse enkelt dag som overlapp' ignoreres).
             */
            should("gi 'true' for udefinerte perioder") {
                DateUtil.overlapper(
                    start1 = null,
                    slutt1 = null,
                    start2 = null,
                    slutt2 = null,
                    anseEnkeltDagSomOverlapp = false
                ) shouldBe true
            }
        }
    }

    context("overlapperEndeloest - endeløs periode 2") {
        val start1 = LocalDate.of(2021, 3, 1)
        val slutt1 = LocalDate.of(2021, 5, 31)

        should("gi 'false' når hele periode 2 er etter periode 1") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = slutt1.plusMonths(1),
                slutt2 = null, // endeløs
                anseEnkeltDagSomOverlapp = false
            ) shouldBe false
        }

        should("gi 'true' når periode 2 dekker slutten av periode 1") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = slutt1.minusMonths(1),
                slutt2 = null, // endeløs
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'true' når periode 2 dekker starten og slutten av periode 1") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = start1.minusMonths(1),
                slutt2 = null, // endeløs
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        context("når enkeltdag skal anses som overlapp") {
            should("gi 'true' når periode 2 overlapper slutten av periode 1 med en enkelt dag") {
                DateUtil.overlapperEndeloest(
                    start1,
                    slutt1,
                    start2 = slutt1, // overlapper en enkelt dag
                    slutt2 = null, // endeløs
                    anseEnkeltDagSomOverlapp = true
                ) shouldBe true
            }
        }

        context("når enkeltdag ikke skal anses som overlapp") {
            should("gi 'false' når periode 2 overlapper slutten av periode 1 med en enkelt dag") {
                DateUtil.overlapperEndeloest(
                    start1,
                    slutt1,
                    start2 = slutt1, // overlapper en enkelt dag
                    slutt2 = null, // endeløs
                    anseEnkeltDagSomOverlapp = false
                ) shouldBe false
            }
        }

        should("gi 'true' når periode 2 har udefinert start") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = null, // anses å starte ved tidens begynnelse
                slutt2 = null, // endeløs
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }
    }

    context("overlapperEndeloest - endeløs periode 1") {
        val start1 = LocalDate.of(2021, 3, 1)
        val slutt1 = null // endeløs

        should("gi 'true' når periodene starter likt") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = start1,
                slutt2 = null, // endeløs
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'true' når periode 2 overlapper slutten av periode 1") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = LocalDate.of(2021, 6, 1),
                slutt2 = LocalDate.of(2021, 8, 31),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }

        should("gi 'true' når periode 2 overlapper starten og slutten av periode 1") {
            DateUtil.overlapperEndeloest(
                start1,
                slutt1,
                start2 = start1.minusMonths(1),
                slutt2 = LocalDate.of(2021, 8, 31),
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }
    }

    /**
     * Når alle datoer er udefinert anses begge perioder å starte ved tidens begynnelse of slutte ved tidens slutt.
     * Dermed overlapper de over et 'uendelig' tidsrom, og flagget 'anse enkelt dag som overlapp' har ingen effekt.
     */
    context("overlapperEndeloest - alle datoer udefinert") {
        should("gi 'true' når enkeltdag skal anses som overlapp") {
            DateUtil.overlapperEndeloest(
                start1 = null,
                slutt1 = null,
                start2 = null,
                slutt2 = null,
                anseEnkeltDagSomOverlapp = true
            ) shouldBe true
        }

        should("gi 'true' når enkeltdag ikke skal anses som overlapp") {
            DateUtil.overlapperEndeloest(
                start1 = null,
                slutt1 = null,
                start2 = null,
                slutt2 = null,
                anseEnkeltDagSomOverlapp = false
            ) shouldBe true
        }
    }

    context("foersteDagNesteMaaned") {
        should("gi første dag i neste måned") {
            DateUtil.foersteDagNesteMaaned(
                LocalDate.of(2024, 12, 1)
            ) shouldBe LocalDate.of(2025, 1, 1)
        }
    }

    context("maanederInnenforAaret") {
        should("gi 12 for helt år") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 1, 1),
                tom = LocalDate.of(2001, 12, 31),
                aar = 2001
            ) shouldBe 12
        }

        should("gi 12 for 1 år + 1 dag") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 1, 1),
                tom = LocalDate.of(2002, 1, 1),
                aar = 2001
            ) shouldBe 12
        }

        should("gi 1 for 1 hel måned") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 2, 1),
                tom = LocalDate.of(2001, 2, 28),
                aar = 2001
            ) shouldBe 1
        }

        should("gi 1 for 2 måneder minus 1 dag") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 2, 1),
                tom = LocalDate.of(2001, 3, 30),
                aar = 2001
            ) shouldBe 1
        }

        should("gi 1 for 2 halve måneder") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 2, 15),
                tom = LocalDate.of(2001, 3, 14),
                aar = 2001
            ) shouldBe 1
        }

        should("gi 0 når perioden ikke overlapper med året") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 1, 1),
                tom = LocalDate.of(2010, 12, 31),
                aar = 2011
            ) shouldBe 0
        }

        should("gi antall overlappende måneder når periode delvis overlapper året") {
            DateUtil.maanederInnenforAaret(
                fom = LocalDate.of(2001, 10, 20),
                tom = LocalDate.of(2002, 3, 15),
                aar = 2001
            ) shouldBe 2 // november og desember 2001
        }
    }

    context("maanederInnenforRestenAvAaret") {
        should("gi 5 med start 1. juni t.o.m. oktober") {
            DateUtil.maanederInnenforRestenAvAaret(
                fom = LocalDate.of(2001, 2, 1),
                nullableTom = LocalDate.of(2001, 10, 31),
                start = LocalDate.of(2001, 6, 1)
            ) shouldBe 5 // f.o.m. juni t.o.m. oktober
        }

        should("gi 7 med start 1. juni, f.o.m. 1. mars, uten angitt slutt") {
            DateUtil.maanederInnenforRestenAvAaret(
                fom = LocalDate.of(2001, 3, 1),
                nullableTom = null, // vil regnes som 31. desember 2001
                start = LocalDate.of(2001, 6, 1)
            ) shouldBe 7 // f.o.m. juni t.o.m. desember
        }

        should("gi 4 med start 1. mars, f.o.m. 1. juni t.o.m. 15. oktober") {
            DateUtil.maanederInnenforRestenAvAaret(
                fom = LocalDate.of(2001, 6, 1),
                nullableTom = LocalDate.of(2001, 10, 15), // delvis oktober teller ikke med
                start = LocalDate.of(2001, 3, 1)
            ) shouldBe 4 // f.o.m. juni t.o.m. september
        }
    }

    context("foersteDag") {
        should("gi 1. januar for angitt år") {
            DateUtil.foersteDag(2001) shouldBe LocalDate.of(2001, 1, 1)
        }
    }
})