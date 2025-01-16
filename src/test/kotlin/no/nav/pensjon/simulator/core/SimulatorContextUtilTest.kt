package no.nav.pensjon.simulator.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidResponse
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon

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

    test("validerOgFerdigstillResponse resets 'trygdetid 67-75'") {
        val response = TrygdetidResponse().apply {
            trygdetid = Trygdetid().apply {
                tt_67_70 = 1
                tt_67_75 = 2
                virkFom = dateAtNoon(2024, 5, 6)
                virkTom = dateAtNoon(2030, 1, 2)
            }
        }
        
        SimulatorContextUtil.validerOgFerdigstillResponse(
            result = response,
            kravGjelderUfoeretrygd = false
        )

        with(response.trygdetid!!) {
            tt_67_70 shouldBe 1
            tt_67_75 shouldBe 0 // reset
            virkFom shouldBe dateAtNoon(2024, 5, 6)
            virkTom shouldBe dateAtNoon(2030, 1, 2)
        }
    }

    test("validerOgFerdigstillResponse undefines 'virkning-datoer' if uføretrygd") {
        val response = TrygdetidResponse().apply {
            trygdetid = Trygdetid().apply {
                tt = 1
                virkFom = dateAtNoon(2024, 5, 6)
                virkTom = dateAtNoon(2030, 1, 2)
            }
        }

        SimulatorContextUtil.validerOgFerdigstillResponse(
            result = response,
            kravGjelderUfoeretrygd = true
        )

        with(response.trygdetid!!) {
            tt shouldBe 1
            virkFom shouldBe null // undefined
            virkTom shouldBe null // ditto
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
