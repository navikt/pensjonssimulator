package no.nav.pensjon.simulator.alderspensjon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.client.AlderspensjonClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate

class AlderspensjonServiceTest : FunSpec({

    test("simulerAlderspensjon bruker 1. i denne/neste måned for uttak") {
        val client = mock(AlderspensjonClient::class.java)

        AlderspensjonService(client).simulerAlderspensjon(
            AlderspensjonSpec(
                pid = Pid("12906498357"),
                gradertUttak = GradertUttakSpec(
                    uttaksgrad = Uttaksgrad.AATTI_PROSENT,
                    fom = LocalDate.of(2030, 1, 2), // skal bli 2030-02-01
                ),
                heltUttakFom = LocalDate.of(2031, 2, 3), // skal bli 2031-03-01
                antallAarUtenlandsEtter16Aar = 1,
                epsHarPensjon = true,
                epsHarInntektOver2G = false,
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        aarligBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 8, 1),
                    )
                ),
                rettTilAfpOffentligDato = LocalDate.of(2019, 10, 11)
            )
        )

        verify(client).simulerAlderspensjon(
            AlderspensjonSpec(
                pid = Pid("12906498357"),
                gradertUttak = GradertUttakSpec(
                    uttaksgrad = Uttaksgrad.AATTI_PROSENT,
                    fom = LocalDate.of(2030, 2, 1),
                ),
                heltUttakFom = LocalDate.of(2031, 3, 1),
                antallAarUtenlandsEtter16Aar = 1,
                epsHarPensjon = true,
                epsHarInntektOver2G = false,
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        aarligBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 8, 1),
                    )
                ),
                rettTilAfpOffentligDato = LocalDate.of(2019, 10, 11)
            )
        )
    }

    test("simulerAlderspensjon gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            simulerAlderspensjon(
                listOf(
                    InntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 1, 2),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden"
    }

    test("simulerAlderspensjon gir feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        val exception = shouldThrow<BadRequestException> {
            simulerAlderspensjon(
                listOf(
                    InntektSpec(
                        aarligBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2025, 1, 1), // samme fom som forrige
                    )
                )
            )
        }

        exception.message shouldBe "To fremtidige inntekter har samme f.o.m.-dato"
    }

    test("simulerAlderspensjon gir feilmelding for negativ inntekt") {
        val exception = shouldThrow<BadRequestException> {
            simulerAlderspensjon(
                listOf(
                    InntektSpec(
                        aarligBeloep = -1,
                        fom = LocalDate.of(2027, 1, 1),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har negativt beløp"
    }
})

private fun simulerAlderspensjon(inntektSpecListe: List<InntektSpec>): AlderspensjonResult {
    val client = mock(AlderspensjonClient::class.java)

    return AlderspensjonService(client).simulerAlderspensjon(
        AlderspensjonSpec(
            pid = Pid("12906498357"),
            gradertUttak = null,
            heltUttakFom = LocalDate.of(2031, 1, 1),
            antallAarUtenlandsEtter16Aar = 0,
            epsHarPensjon = false,
            epsHarInntektOver2G = false,
            fremtidigInntektListe = inntektSpecListe,
            rettTilAfpOffentligDato = null
        )
    )
}
