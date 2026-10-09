package no.nav.pensjon.simulator.opptjening.client.popp.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSpec
import no.nav.pensjon.simulator.testutil.TestObjects.pid

class PoppOpptjeningsgrunnlagSpecTest : ShouldSpec({

    context("fromInternalValue") {
        should("mappe tom grunnlagstypeliste til udefinert verdi") {
            PoppOpptjeningsgrunnlagSpec.fromInternalValue(
                OpptjeningsgrunnlagSpec(
                    pid = pid,
                    grunnlagstypeListe = emptyList()
                )
            ) shouldBe PoppOpptjeningsgrunnlagSpec(
                fnr = pid.value,
                grunnlagstypeListe = null,
                fomAr = null,
                tomAr = null
            )
        }
    }
})