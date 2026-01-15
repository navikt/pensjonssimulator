package no.nav.pensjon.simulator.alderspensjon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.ProblemType
import java.time.LocalDate

class AlderspensjonServiceTest : ShouldSpec({

    should("gi feilmelding for inntekt som ikke starter 1. i måneden") {
        shouldThrow<BadSpecException> {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 1, 2),
                    )
                )
            )
        } shouldBe BadSpecException(
            message = "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden",
            problemType = ProblemType.UGYLDIG_INNTEKT
        )
    }

    should("gi feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        shouldThrow<BadSpecException> {
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
            )
        } shouldBe BadSpecException(
            message = "To fremtidige inntekter har samme f.o.m.-dato",
            problemType = ProblemType.UGYLDIG_INNTEKT
        )
    }

    should("gi feilmelding for negativ inntekt") {
        shouldThrow<BadSpecException> {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = -1,
                        fom = LocalDate.of(2027, 1, 1),
                    )
                )
            )
        } shouldBe BadSpecException(
            message = "En fremtidig inntekt har negativt beløp",
            problemType = ProblemType.UGYLDIG_INNTEKT
        )
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
