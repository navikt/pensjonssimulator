package no.nav.pensjon.simulator.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

class SimulatorContextUtilTest : FunSpec({

    test("finishOpptjeningInit resets poengtall values except 'veiet grunnbeløp' and 'uforeår'") {
        val pensjonsbeholdning = pensjonsbeholdning()

        SimulatorContextUtil.finishOpptjeningInit(ArrayList(listOf(pensjonsbeholdning)))

        with(pensjonsbeholdning.opptjening?.poengtall!!) {
            gv shouldBe 1 // 'veiet grunnbeløp' retained
            uforear shouldBe true // 'uforeår' retained
            ar shouldBe 0 // value has been reset
            pp shouldBe 0.0 // value has been reset
        }
    }
})

private fun pensjonsbeholdning() =
    Pensjonsbeholdning().apply {
        opptjening = Opptjening().apply {
            poengtall = Poengtall().apply {
                gv = 1
                uforear = true
                ar = 3
                pp = 1.2
            }
        }
    }
