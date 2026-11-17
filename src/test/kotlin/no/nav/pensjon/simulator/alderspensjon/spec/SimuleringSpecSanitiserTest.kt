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

    context("sluttdato for inntekt etter helt uttak blir tidligere enn endret uttaksdato") {
        context("gradert uttak") {
            should("sette sluttdato for inntekt lik dato for helt uttak") {
                SimuleringSpecSanitiser.sanitise(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, Month.JANUARY, 2),
                        uttaksgrad = UttakGradKode.P_50,
                        heltUttakDato = LocalDate.of(2035, Month.APRIL, 3),
                        inntektEtterHeltUttakTom = LocalDate.of(2030, Month.JANUARY, 3)
                    )
                ) shouldBe simuleringSpec(
                    foersteUttakDato = LocalDate.of(2030, Month.FEBRUARY, 1),
                    uttaksgrad = UttakGradKode.P_50,
                    heltUttakDato = LocalDate.of(2035, Month.MAY, 1),
                    inntektEtterHeltUttakTom = LocalDate.of(2035, Month.MAY, 1)
                )
            }
        }

        context("ugradert uttak") {
            should("sette sluttdato for inntekt lik dato for første uttak") {
                SimuleringSpecSanitiser.sanitise(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, Month.JANUARY, 2),
                        uttaksgrad = UttakGradKode.P_100,
                        heltUttakDato = null, // ugradert uttak
                        inntektEtterHeltUttakTom = LocalDate.of(2030, Month.JANUARY, 3)
                    )
                ) shouldBe simuleringSpec(
                    foersteUttakDato = LocalDate.of(2030, Month.FEBRUARY, 1),
                    uttaksgrad = UttakGradKode.P_100,
                    heltUttakDato = null,
                    inntektEtterHeltUttakTom = LocalDate.of(2030, Month.FEBRUARY, 1)
                )
            }
        }
    }
})