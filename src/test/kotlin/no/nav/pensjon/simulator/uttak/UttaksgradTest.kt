package no.nav.pensjon.simulator.uttak

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UttaksgradTest : FunSpec({

    test("'from' should return uttaksgrad if argument is a valid prosentsats") {
        Uttaksgrad.from(20) shouldBe Uttaksgrad.TJUE_PROSENT
    }

    test("'from' should throw IllegalArgumentException if argument is an invalid prosentsats") {
        shouldThrow<IllegalArgumentException> {
            Uttaksgrad.from(21)
        }.message shouldBe "Ugyldig prosentsats for uttaksgrad: 21"
    }
})
