package no.nav.pensjon.simulator.alderspensjon.spec

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.ProblemType
import java.time.LocalDate

class SimuleringSpecValidatorTest : ShouldSpec({

    should("throw exception if: En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden") {
        shouldThrow<BadSpecException> {
            SimuleringSpecValidator.validate(
                spec = simuleringSpec(
                    inntektSpecListe = listOf(
                        FremtidigInntekt(
                            aarligInntektBeloep = 1,
                            fom = LocalDate.of(2025, 1, 2) // ikke den 1. i måneden
                        )
                    )
                ),
                today = LocalDate.of(2025, 1, 1)
            )
        } shouldBe BadSpecException(
            message = "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden",
            problemType = ProblemType.UGYLDIG_INNTEKT
        )
    }

    should("throw exception if: To fremtidige inntekter har samme f.o.m.-dato") {
        shouldThrow<BadSpecException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(
                    inntektSpecListe = listOf(
                        FremtidigInntekt(
                            aarligInntektBeloep = 1,
                            fom = LocalDate.of(2025, 1, 1)
                        ),
                        FremtidigInntekt(
                            aarligInntektBeloep = 2,
                            fom = LocalDate.of(2025, 1, 1) // samme f.o.m.-dato
                        )
                    )
                ),
                today = LocalDate.of(2025, 1, 1)
            )
        } shouldBe BadSpecException(
            message = "To fremtidige inntekter har samme f.o.m.-dato",
            problemType = ProblemType.UGYLDIG_INNTEKT
        )
    }

    should("throw exception if: Dato for første uttak mangler") {
        shouldThrow<BadSpecException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(foersteUttakDato = null),
                today = LocalDate.of(2025, 1, 1)
            )
        } shouldBe BadSpecException(
            message = "Dato for første uttak mangler",
            problemType = ProblemType.UGYLDIG_UTTAKSDATO
        )
    }

    should("throw exception if: Dato for første uttak er før dagens dato") {
        shouldThrow<BadSpecException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(foersteUttakDato = LocalDate.of(2025, 1, 1)),
                today = LocalDate.of(2025, 1, 2)
            )
        } shouldBe BadSpecException(
            message = "Dato for første uttak (2025-01-01) er for tidlig",
            problemType = ProblemType.UGYLDIG_UTTAKSDATO
        )
    }

    should("throw exception if: Andre uttak (100 %) starter ikke etter første uttak (gradert)") {
        shouldThrow<BadSpecException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(
                    foersteUttakDato = LocalDate.of(2032, 6, 2)
                    // heltUttakDato = LocalDate.of(2032, 6, 1) // starter ikke etter første uttak
                ),
                today = LocalDate.of(2025, 1, 1)
            )
        } shouldBe BadSpecException(
            message = "Andre uttak (100 %) starter ikke etter første uttak (gradert)",
            problemType = ProblemType.UGYLDIG_UTTAKSDATO
        )
    }
})
