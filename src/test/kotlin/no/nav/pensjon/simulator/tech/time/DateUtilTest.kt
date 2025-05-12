package no.nav.pensjon.simulator.tech.time

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DateUtilTest : FunSpec({

    test("toLocalDate takes time zone into account") {
        DateUtil.toLocalDate(
            ZonedDateTime.of(
                2024, 12, 31, 23, 0, 0, 0,
                ZoneId.of("UTC") // 1 hour difference compared to Norwegian time zone
            )
        ) shouldBe LocalDate.of(2025, 1, 1)
    }

    test("foersteDagNesteMaaned gir første dag i neste måned") {
        DateUtil.foersteDagNesteMaaned(
            LocalDate.of(2024, 12, 1)
        ) shouldBe LocalDate.of(2025, 1, 1)
    }

    test("maanederInnenforAaret for helt år gir 12") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 1, 1),
            tom = LocalDate.of(2001, 12, 31),
            aar = 2001
        ) shouldBe 12
    }

    test("maanederInnenforAaret for 1 år + 1 dag gir 12") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 1, 1),
            tom = LocalDate.of(2002, 1, 1),
            aar = 2001
        ) shouldBe 12
    }

    test("maanederInnenforAaret for 1 hel måned gir 1") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 2, 1),
            tom = LocalDate.of(2001, 2, 28),
            aar = 2001
        ) shouldBe 1
    }

    test("maanederInnenforAaret for 2 måneder minus 1 dag gir 1") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 2, 1),
            tom = LocalDate.of(2001, 3, 30),
            aar = 2001
        ) shouldBe 1
    }

    test("maanederInnenforAaret for 2 halve måneder gir 1") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 2, 15),
            tom = LocalDate.of(2001, 3, 14),
            aar = 2001
        ) shouldBe 1
    }

    test("maanederInnenforAaret uten overlapp gir 0") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 1, 1),
            tom = LocalDate.of(2010, 12, 31),
            aar = 2011
        ) shouldBe 0
    }

    test("maanederInnenforAaret med delvis overlapp gir antall overlappende måneder") {
        DateUtil.maanederInnenforAaret(
            fom = LocalDate.of(2001, 10, 20),
            tom = LocalDate.of(2002, 3, 15),
            aar = 2001
        ) shouldBe 2 // november og desember 2001
    }

    test("maanederInnenforRestenAvAaret med start 1. juni t.o.m. oktober gir 5") {
        DateUtil.maanederInnenforRestenAvAaret(
            fom = LocalDate.of(2001, 2, 1),
            nullableTom = LocalDate.of(2001, 10, 31),
            start = LocalDate.of(2001, 6, 1)
        ) shouldBe 5 // f.o.m. juni t.o.m. oktober
    }

    test("maanederInnenforRestenAvAaret med start 1. juni, f.o.m. 1. mars, uten angitt slutt gir 7") {
        DateUtil.maanederInnenforRestenAvAaret(
            fom = LocalDate.of(2001, 3, 1),
            nullableTom = null, // vil regnes som 31. desember 2001
            start = LocalDate.of(2001, 6, 1)
        ) shouldBe 7 // f.o.m. juni t.o.m. desember
    }

    test("maanederInnenforRestenAvAaret med start 1. mars, f.o.m. 1. juni t.o.m. 15. oktober gir 4") {
        DateUtil.maanederInnenforRestenAvAaret(
            fom = LocalDate.of(2001, 6, 1),
            nullableTom = LocalDate.of(2001, 10, 15), // delvis oktober teller ikke med
            start = LocalDate.of(2001, 3, 1)
        ) shouldBe 4 // f.o.m. juni t.o.m. september
    }

    test("foersteDag should return 1. januar for angitt år") {
        DateUtil.foersteDag(2001) shouldBe LocalDate.of(2001, 1, 1)
    }
})
