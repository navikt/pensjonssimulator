package no.nav.pensjon.simulator.core.domain.regler.enum

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class LandkodeEnumTest : ShouldSpec({

    should("konvertere landkode til enum") {
        LandkodeEnum.extendedValueOf("NOR") shouldBe LandkodeEnum.NOR
        LandkodeEnum.extendedValueOf("SWE") shouldBe LandkodeEnum.SWE
        LandkodeEnum.extendedValueOf("USA") shouldBe LandkodeEnum.USA
        LandkodeEnum.extendedValueOf("CAN") shouldBe LandkodeEnum.CAN
        LandkodeEnum.extendedValueOf("FIN") shouldBe LandkodeEnum.FIN
        LandkodeEnum.extendedValueOf("DNK") shouldBe LandkodeEnum.DNK
        LandkodeEnum.extendedValueOf("???") shouldBe LandkodeEnum.P_UKJENT
        LandkodeEnum.extendedValueOf("349") shouldBe LandkodeEnum.P_SPANSKE_OMR_AFRIKA
        LandkodeEnum.extendedValueOf("546") shouldBe LandkodeEnum.P_SIKKIM
        LandkodeEnum.extendedValueOf("556") shouldBe LandkodeEnum.P_YEMEN
        LandkodeEnum.extendedValueOf("669") shouldBe LandkodeEnum.P_PANAMAKANALSONEN
    }
})
