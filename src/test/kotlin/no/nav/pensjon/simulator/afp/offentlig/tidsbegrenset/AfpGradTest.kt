package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.g.GrunnbeloepService
import java.time.LocalDate

class AfpGradTest : ShouldSpec({

    context("inntekt ved AFP-uttak mindre enn toleransebeløpet") {
        should("gi full AFP-grad (100)") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = TOLERANSEBELOEP_2025 - 1,
                tidligereInntekt = 50000
            ) shouldBe 100

            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 0,
                tidligereInntekt = 50000
            ) shouldBe 100

            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = -50000,
                tidligereInntekt = 50000
            ) shouldBe 100
        }
    }

    context("inntekt ved AFP-uttak lik toleransebeløpet") {
        should("gi full AFP-grad (100)") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = TOLERANSEBELOEP_2025,
                tidligereInntekt = 50000
            ) shouldBe 100
        }
    }

    context("inntekt ved AFP-uttak større enn toleransebeløpet") {
        should("gi redusert AFP-grad") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = TOLERANSEBELOEP_2025 + 1,
                tidligereInntekt = 64494
            ) shouldBe 50 // 100 - 32247/64494 * 100 = 50

            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 100000,
                tidligereInntekt = 100000
            ) shouldBe 0
        }

        should("avrunde til nærmeste heltall") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 50500,
                tidligereInntekt = 100000
            ) shouldBe 50 // 100 - 50500/100000 * 100 = 49.5 -> 50

           AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
               aar = 2025,
               inntektVedAfpUttak = 50501,
               tidligereInntekt = 100000
           ) shouldBe 49 // 100 - 50501/100000 * 100 = 49.499 -> 49
        }
    }

    context("inntekt ved AFP-uttak større enn tidligere tnntekt") {
        should("gi AFP-grad 0") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 100000,
                tidligereInntekt = 75000
            ) shouldBe 0
        }
    }

    context("2026") {
        should("bruke grunnbeløpet som gjaldt 1. januar 2026") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2026,
                inntektVedAfpUttak = TOLERANSEBELOEP_2026 - 1,
                tidligereInntekt = 50000
            ) shouldBe 100
        }
    }

    context("tidligere inntekt ikke-positiv") {
        should("gi AFP-grad 0") {
            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 1,
                tidligereInntekt = -50000
            ) shouldBe 0

            AfpGrad(grunnbeloepService = arrangeGrunnbeloep).beregnAfpGrad(
                aar = 2025,
                inntektVedAfpUttak = 1,
                tidligereInntekt = 0
            ) shouldBe 0
        }
    }
})

/**
 * Ref. www.nav.no/afp-offentlig#inntekt-afp
 */
private const val TOLERANSEBELOEP_2025: Int = 32247 // 124028 * .26 = 32247.28 -> 32247
private const val TOLERANSEBELOEP_2026: Int = 33842 // 130160 * .26 = 33841.60 -> 33842

private val arrangeGrunnbeloep: GrunnbeloepService =
    mockk {
        every { grunnbeloep(LocalDate.of(2025, 1, 1)) } returns 124028
        every { grunnbeloep(LocalDate.of(2026, 1, 1)) } returns 130160
    }