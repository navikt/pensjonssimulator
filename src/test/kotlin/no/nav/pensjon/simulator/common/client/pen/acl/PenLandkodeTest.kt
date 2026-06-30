package no.nav.pensjon.simulator.common.client.pen.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum

class PenLandkodeTest : ShouldSpec({

    should("map 'Svalbard og Jan Mayen V1' to 'Svalbard og Jan Mayen") {
        PenLandkode.internalValue(externalValue = "SJM_V1") shouldBe LandkodeEnum.SJM
    }

    should("map missing value to special internal value 'uoppgitt/ukjent'") {
        PenLandkode.internalValue(externalValue = "") shouldBe LandkodeEnum.XUK
    }

    should("map unknown value to special internal value 'uoppgitt/ukjent'") {
        PenLandkode.internalValue(externalValue = "HVA_BEHAGER") shouldBe LandkodeEnum.XUK
    }
})