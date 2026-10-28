package no.nav.pensjon.simulator.alderspensjon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException

class UttaksgradTest : ShouldSpec({

    context("funksjonen 'from'") {
        context("gyldig prosentsats") {
            should("gi uttaksgrad for angitt prosentsats") {
                Uttaksgrad.from(prosentsats = 20) shouldBe Uttaksgrad.TJUE_PROSENT
            }
        }

        context("ugyldig prosentsats") {
            should("gi feilmelding") {
                shouldThrow<InvalidEnumValueException> {
                    Uttaksgrad.from(prosentsats = 10)
                }.message shouldBe "Ugyldig prosentsats for uttaksgrad: 10"

                shouldThrow<InvalidEnumValueException> {
                    Uttaksgrad.from(prosentsats = null)
                }.message shouldBe "Ugyldig prosentsats for uttaksgrad: null"
            }
        }
    }
})