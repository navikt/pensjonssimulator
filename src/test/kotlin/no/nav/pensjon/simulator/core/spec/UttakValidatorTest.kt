package no.nav.pensjon.simulator.core.spec

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

class UttakValidatorTest : ShouldSpec({

    should("feilmelde at dato for første uttak mangler") {
        shouldThrow<BadSpecException> {
            UttakValidator.validateGradertUttak(simuleringSpec(foersteUttakDato = null))
        }.message shouldBe "dato for første uttak mangler"
    }

    should("feilmelde at dato for helt uttak mangler") {
        shouldThrow<BadSpecException> {
            UttakValidator.validateGradertUttak(simuleringSpec(heltUttakDato = null))
        }.message shouldBe "dato for helt uttak mangler (obligatorisk ved gradert uttak)"
    }

    should("feilmelde at dato for første uttak ikke er før dato for helt uttak") {
        shouldThrow<BadSpecException> {
            UttakValidator.validateGradertUttak(
                simuleringSpec(
                    foersteUttakDato = LocalDate.of(2030, 1, 1),
                    heltUttakDato = LocalDate.of(2030, 1, 1)
                )
            )
        }.message shouldBe "dato for første uttak (2030-01-01) er ikke før dato for helt uttak (2030-01-01)"
    }
})
