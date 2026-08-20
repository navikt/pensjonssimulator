package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.g.GrunnbeloepService
import java.time.LocalDate

class AfpGradTest : ShouldSpec({

    context("tidligere inntekt = 0") {
        should("gi AFP-grad 0") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 1,
                tidligereInntekt = 0
            ) shouldBe 0
        }
    }

    context("inntekt mindre enn toleransebeløpet") {
        should("gi full AFP-grad (100)") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 33841,
                tidligereInntekt = 50000
            ) shouldBe 100
        }
    }

    context("inntekt lik toleransebeløpet") {
        should("gi full AFP-grad (100)") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 33842, // 130160 * .26 = 33841.6 -> toleransebeløp 33842
                tidligereInntekt = 50000
            ) shouldBe 100
        }
    }

    context("inntekt større enn toleransebeløpet") {
        should("gi redusert AFP-grad") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 33843,
                tidligereInntekt = 50000
            ) shouldBe 33
        }
    }

    context("2026") {
        should("bruke grunnbeløpet som gjaldt 1. januar 2026") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2026,
                inntektVedAfpUttak = 35502,
                tidligereInntekt = 50000
            ) shouldBe 100
        }
    }
})

private val arrangeGrunnbeloep: GrunnbeloepService =
    mockk {
        every { grunnbeloep(LocalDate.of(2025, 1, 1)) } returns 130160
        every { grunnbeloep(LocalDate.of(2026, 1, 1)) } returns 136549
    }