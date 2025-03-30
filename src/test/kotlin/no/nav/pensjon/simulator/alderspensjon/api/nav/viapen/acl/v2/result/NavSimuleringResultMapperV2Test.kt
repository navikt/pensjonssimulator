package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertOpptjening

class NavSimuleringResultMapperV2Test : FunSpec({

    test("toSimuleringResultV2 should use null as default for pensjonspoengPi") {
        NavSimuleringResultMapperV2.toSimuleringResultV2(
            SimulatorOutput().apply {
                opptjeningListe.add(SimulertOpptjening(pensjonsgivendeInntektPensjonspoeng = null))
            }
        ).opptjeningListe[0].pensjonspoengPi shouldBe null
    }
})
