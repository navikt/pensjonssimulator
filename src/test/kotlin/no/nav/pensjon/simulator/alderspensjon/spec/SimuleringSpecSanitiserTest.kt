package no.nav.pensjon.simulator.alderspensjon.spec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativtUttakServiceTestObjects.simuleringSpec
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate
import java.time.Month

class SimuleringSpecSanitiserTest : ShouldSpec({

    context("uttaksdag ikke første dag i måneden") {
        should("justere uttaksdato til første dag i påfølgende måned") {
            SimuleringSpecSanitiser.sanitise(
                simuleringSpec(
                    foersteUttakDato = LocalDate.of(2030, Month.JANUARY, 2),
                    uttaksgrad = UttakGradKode.P_50,
                    heltUttakDato = LocalDate.of(2035, Month.APRIL, 3)
                )
            ) shouldBe simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, Month.FEBRUARY, 1),
                uttaksgrad = UttakGradKode.P_50,
                heltUttakDato = LocalDate.of(2035, Month.MAY, 1)
            )
        }
    }
})