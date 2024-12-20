package no.nav.pensjon.simulator.core.inntekt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import java.math.BigInteger
import java.time.LocalDate

class InntektUtilTest : FunSpec({

    test("faktiskAarligInntekt = 0 når ingen inntekter") {
        InntektUtil.faktiskAarligInntekt(emptyList()) shouldBe BigInteger.ZERO
    }

    test("faktiskAarligInntekt = full årlig verdi når inntekt starter 1. januar") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 1, 1))
            )
        )

        actual shouldBe BigInteger.valueOf(120_000L)
    }

    test("faktiskAarligInntekt = full årlig verdi når inntekt starter 31. januar") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 1, 31))
            )
        )

        actual shouldBe BigInteger.valueOf(120_000L)
    }

    test("faktiskAarligInntekt = 1/12 av årlig verdi når inntekt starter 1. desember") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 12, 1))
            )
        )

        actual shouldBe BigInteger.valueOf(10000L)
    }

    test("faktiskAarligInntekt = 1/12 av årlig verdi når inntekt starter 31. desember") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 12, 31))
            )
        )

        actual shouldBe BigInteger.valueOf(10000L)
    }

    test("faktiskAarligInntekt = siste inntekt i listen når de starter samme dag") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 1, 1)),
                FremtidigInntekt(aarligInntektBeloep = 110_000, fom = LocalDate.of(2021, 1, 1))
            )
        )

        actual shouldBe BigInteger.valueOf(110_000L)
    }

    test("faktiskAarligInntekt = siste inntekt i listen når de starter innenfor samme måned") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 1, 31)),
                FremtidigInntekt(aarligInntektBeloep = 110_000, fom = LocalDate.of(2021, 1, 1)),
                FremtidigInntekt(aarligInntektBeloep = 130_000, fom = LocalDate.of(2021, 1, 15))
            )
        )

        actual shouldBe BigInteger.valueOf(130_000L)
    }

    test("faktiskAarligInntekt = sum av periodiserte inntekter") {
        val actual = InntektUtil.faktiskAarligInntekt(
            listOf(
                FremtidigInntekt(aarligInntektBeloep = 120_000, fom = LocalDate.of(2021, 1, 1)),
                FremtidigInntekt(aarligInntektBeloep = 110_000, fom = LocalDate.of(2021, 7, 1))
            )
        )

        actual shouldBe BigInteger.valueOf(115_000L)
    }
})
