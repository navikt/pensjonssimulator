package no.nav.pensjon.simulator.alderspensjon.spec

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class SimuleringSpecValidatorTest : FunSpec({

    test("'validate' should throw exception if: En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden") {
        shouldThrow<BadRequestException> {
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
        }.message shouldBe "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden"
    }

    test("'validate' should throw exception if: To fremtidige inntekter har samme f.o.m.-dato") {
        shouldThrow<BadRequestException> {
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
        }.message shouldBe "To fremtidige inntekter har samme f.o.m.-dato"
    }

    test("'validate' should throw exception if: Dato for første uttak mangler") {
        shouldThrow<BadRequestException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(foersteUttakDato = null),
                today = LocalDate.of(2025, 1, 1)
            )
        }.message shouldBe "Dato for første uttak mangler"
    }

    test("'validate' should throw exception if: Dato for første uttak er før dagens dato") {
        shouldThrow<BadRequestException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(foersteUttakDato = LocalDate.of(2025, 1, 31)),
                today = LocalDate.of(2025, 2, 1)
            )
        }.message shouldBe "Dato for første uttak er for tidlig"
    }

    test("'validate' should throw exception if: Andre uttak (100 %) starter ikke etter første uttak (gradert)") {
        shouldThrow<BadRequestException> {
            SimuleringSpecValidator.validate(
                simuleringSpec(
                    foersteUttakDato = LocalDate.of(2032, 6, 2)
                    // heltUttakDato = LocalDate.of(2032, 6, 1) // starter ikke etter første uttak
                ),
                today = LocalDate.of(2025, 1, 1)
            )
        }.message shouldBe "Andre uttak (100 %) starter ikke etter første uttak (gradert)"
    }
})
