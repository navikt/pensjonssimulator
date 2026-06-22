package no.nav.pensjon.simulator.person

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class FoedselsnummerUtilTest : ShouldSpec({

    should("redaktere fødselsnummer slik at bare de 4 tallene som representerer måned og år forblir synlige") {
        FoedselsnummerUtil.redact("15126512345") shouldBe "**1265*****"
    }
})