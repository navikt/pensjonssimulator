package no.nav.pensjon.simulator.core.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class SimuleringSpecTest : FunSpec({

    test("hasSameUttakAs should be true if same dates are given for foersteUttakDato and heltUttakDato respectively") {
        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe true
    }

    test("hasSameUttakAs should be false if dates are respectively different") {
        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 2),
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe false

        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                heltUttakDato = LocalDate.of(2033, 6, 1)
            )
        ) shouldBe false
    }

    test("hasSameUttakAs should be true if all dates are null") {
        simuleringSpec(
            foersteUttakDato = null,
            heltUttakDato = null
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = null,
                heltUttakDato = null
            )
        ) shouldBe true
    }

    test("hasSameUttakAs should be false if null vs non-null") {
        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = null
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe false

        simuleringSpec(
            foersteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2032, 6, 1)
        ).hasSameUttakAs(
            simuleringSpec(
                foersteUttakDato = null,
                heltUttakDato = LocalDate.of(2032, 6, 1)
            )
        ) shouldBe false
    }
})
