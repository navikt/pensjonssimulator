package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AfpGradTest : ShouldSpec({

    context("tidligere inntekt = 0") {
        should("gi AFP-grad 0") {
            AfpGrad.beregnAfpGrad(
                inntektVedAfpUttak = 1,
                tidligereInntekt = 0,
                grunnbeloep = 130160
            ) shouldBe 0
        }
    }

    context("inntekt mindre enn toleransebeløpet") {
        should("gi full AFP-grad (100)") {
            AfpGrad.beregnAfpGrad(
                inntektVedAfpUttak = 33841,
                tidligereInntekt = 50000,
                grunnbeloep = 130160
            ) shouldBe 100
        }
    }

    context("inntekt lik toleransebeløpet") {
        should("gi full AFP-grad (100)") {
            AfpGrad.beregnAfpGrad(
                inntektVedAfpUttak = 33842, // 130160 * .26 = 33841.6 -> toleransebeløp 33842
                tidligereInntekt = 50000,
                grunnbeloep = 130160
            ) shouldBe 100
        }
    }

    context("inntekt større enn toleransebeløpet") {
        should("gi redusert AFP-grad") {
            AfpGrad.beregnAfpGrad(
                inntektVedAfpUttak = 33843,
                tidligereInntekt = 50000,
                grunnbeloep = 130160
            ) shouldBe 33
        }
    }
})