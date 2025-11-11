package no.nav.pensjon.simulator.core.trygd

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum

class TrygdetidOpptjeningRettLandTest : ShouldSpec({

    context("USA") {
        should("ha rett til opptjening av trygdetid hvis arbeidet der") {
            TrygdetidOpptjeningRettLand.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.USA,
                harArbeidet = true
            ) shouldBe true
        }

        should("ikke ha rett til opptjening av trygdetid hvis ikke arbeidet der") {
            TrygdetidOpptjeningRettLand.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.USA,
                harArbeidet = false
            ) shouldBe false
        }
    }

    context("Åland") {
        should("ha rett til opptjening av trygdetid uten krav om arbeid") {
            TrygdetidOpptjeningRettLand.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.ALA,
                harArbeidet = false
            ) shouldBe true
        }
    }
})
