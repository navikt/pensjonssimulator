package no.nav.pensjon.simulator.core.domain.regler.enum

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AvtaleLandEnumTest : ShouldSpec({

    context("Finland") {
        should("ha rett til opptjening av trygdetid uten krav om arbeid") {
            AvtaleLandEnum.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.FIN,
                harArbeidet = false
            ) shouldBe true
        }
    }

    context("USA") {
        should("ha rett til opptjening av trygdetid hvis arbeidet der") {
            AvtaleLandEnum.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.USA,
                harArbeidet = true
            ) shouldBe true
        }

        should("ikke ha rett til opptjening av trygdetid hvis ikke arbeidet der") {
            AvtaleLandEnum.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.USA,
                harArbeidet = false
            ) shouldBe false
        }
    }

    context("Tyrkia") {
        should("ikke ha rett til opptjening av trygdetid") {
            AvtaleLandEnum.rettTilOpptjeningAvTrygdetid(
                land = LandkodeEnum.TUR,
                harArbeidet = true
            ) shouldBe false
        }
    }
})