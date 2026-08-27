package no.nav.pensjon.simulator.tid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class AarOgMaanedTest : ShouldSpec({

    context("forDato") {
        should("gi år og måned (1..12) i henhold til datoen") {
            AarOgMaaned.forDato(LocalDate.of(2022, 1, 15)) shouldBe
                    AarOgMaaned(aar = 2022, maaned = 1)
        }
    }

    context("erEtter") {
        should("gi true hvis etter, false hvis likt eller før") {
            AarOgMaaned(aar = 2022, maaned = 12).erEtter(AarOgMaaned(aar = 2023, maaned = 1)) shouldBe false
            AarOgMaaned(aar = 2022, maaned = 1).erEtter(AarOgMaaned(aar = 2022, maaned = 2)) shouldBe false
            AarOgMaaned(aar = 2022, maaned = 1).erEtter(AarOgMaaned(aar = 2022, maaned = 1)) shouldBe false
            AarOgMaaned(aar = 2022, maaned = 2).erEtter(AarOgMaaned(aar = 2022, maaned = 1)) shouldBe true
            AarOgMaaned(aar = 2023, maaned = 1).erEtter(AarOgMaaned(aar = 2022, maaned = 12)) shouldBe true
        }
    }
})