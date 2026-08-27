package no.nav.pensjon.simulator.tid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class OverlappTest : ShouldSpec({

    context("f.o.m-dato etter t.o.m-dato") {
        should("gi 0") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2001, maaned = 2),
                tom = AarOgMaaned(aar = 2001, maaned = 1)
            ) shouldBe 0
        }
    }

    context("intet overlapp") {
        should("gi 0") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2002, maaned = 1),
                tom = AarOgMaaned(aar = 2002, maaned = 12)
            ) shouldBe 0
        }
    }

    context("eksakt overlapp") {
        should("gi 12") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2001, maaned = 1),
                tom = AarOgMaaned(aar = 2001, maaned = 12)
            ) shouldBe 12
        }
    }

    context("overlapp med dobbeltsidig margin") {
        should("gi 12") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2000, maaned = 1),
                tom = AarOgMaaned(aar = 2002, maaned = 12)
            ) shouldBe 12
        }
    }

    context("3 måneders eksakt overlapp ved årets start") {
        should("gi 3") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2001, maaned = 1),
                tom = AarOgMaaned(aar = 2001, maaned = 3)
            ) shouldBe 3
        }
    }

    context("8 måneders eksakt overlapp ved årets slutt") {
        should("gi 8") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2001, maaned = 5),
                tom = AarOgMaaned(aar = 2001, maaned = 12)
            ) shouldBe 8
        }
    }

    context("1 måneds overlapp midt i året") {
        should("gi 1") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2001, maaned = 6),
                tom = AarOgMaaned(aar = 2001, maaned = 6)
            ) shouldBe 1
        }
    }

    context("2 måneders overlapp ved årets start med enkeltsidig margin") {
        should("gi 2") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2000, maaned = 12),
                tom = AarOgMaaned(aar = 2001, maaned = 2)
            ) shouldBe 2
        }
    }

    context("4 måneders overlapp ved årets slutt med enkeltsidig margin") {
        should("gi 4") {
            Overlapp.antallMaaneder(
                aar = 2001,
                fom = AarOgMaaned(aar = 2001, maaned = 9),
                tom = AarOgMaaned(aar = 2003, maaned = 1)
            ) shouldBe 4
        }
    }
})
