package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AfpOffentligLivsvarigDtoTest : FunSpec({

    test("toAfpLivsvarig maps bruttoPerAr") {
        AfpOffentligLivsvarigDto(1.2).toAfpLivsvarig().bruttoPerAr shouldBe 1.2
    }
})
