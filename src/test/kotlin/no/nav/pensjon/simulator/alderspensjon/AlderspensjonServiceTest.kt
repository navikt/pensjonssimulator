package no.nav.pensjon.simulator.alderspensjon

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import java.time.LocalDate

class AlderspensjonServiceTest : ShouldSpec({

    context("inntekt som ikke starter 1. i måneden") {
        should("inkludere problembeskrivelse i returverdien") {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 1, 2),
                    )
                )
            ).problem shouldBe Problem(
                type = ProblemType.UGYLDIG_INNTEKT,
                beskrivelse = "BadSpecException"
            )
        }
    }

    context("inntekter med ikke-unik f.o.m.-dato") {
        should("inkludere problembeskrivelse i returverdien") {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1),
                    ),
                    PensjonInntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2025, 1, 1), // samme fom som forrige
                    )
                )
            ).problem shouldBe Problem(
                type = ProblemType.UGYLDIG_INNTEKT,
                beskrivelse = "BadSpecException"
            )
        }
    }

    context("negativ inntekt") {
        should("inkludere problembeskrivelse i returverdien") {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = -1,
                        fom = LocalDate.of(2027, 1, 1),
                    )
                )
            ).problem shouldBe Problem(
                type = ProblemType.UGYLDIG_INNTEKT,
                beskrivelse = "BadSpecException"
            )
        }
    }
})

private fun simulerAlderspensjon(inntektSpecListe: List<PensjonInntektSpec>): AlderspensjonResult =
    AlderspensjonService(
        simulator = mockk(),
        alternativSimuleringService = mockk(),
        personService = Arrange.foedselsdato(1963, 1, 1),
        simuleringstypeDeducer = Arrange.simuleringstype(
            type = SimuleringTypeEnum.ALDER,
            uttakFom = LocalDate.of(2027, 1, 1),
            livsvarigOffentligAfpRettFom = null
        ),
        outputConverter = mockk(),
        time = { LocalDate.of(2025, 1, 1) }
    ).simulerAlderspensjon(
        AlderspensjonSpec(
            pid,
            gradertUttak = null,
            heltUttakFom = LocalDate.of(2027, 1, 1),
            antallAarUtenlandsEtter16 = 0,
            epsHarPensjon = false,
            epsHarInntektOver2G = false,
            fremtidigInntektListe = inntektSpecListe,
            livsvarigOffentligAfpRettFom = null
        )
    )