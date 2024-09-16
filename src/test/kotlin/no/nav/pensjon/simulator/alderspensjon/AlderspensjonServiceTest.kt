package no.nav.pensjon.simulator.alderspensjon

import io.kotest.core.spec.style.FunSpec
import no.nav.pensjon.simulator.alderspensjon.client.AlderspensjonClient
import no.nav.pensjon.simulator.person.Pid
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate

class AlderspensjonServiceTest : FunSpec({

    test("simulerAlderspensjon bruker 1. i denne/neste måned for uttak og inntekt") {
        val client = mock(AlderspensjonClient::class.java)

        AlderspensjonService(client).simulerAlderspensjon(
            AlderspensjonSpec(
                pid = Pid("12906498357"),
                gradertUttak = GradertUttakSpec(
                    uttaksgrad = Uttaksgrad.AATTI_PROSENT,
                    fom = LocalDate.of(2030, 1, 2), // skal bli 2030-02-01
                ),
                heltUttakFom = LocalDate.of(2031, 2, 3), // skal bli 2030-03-01
                antallAarUtenlandsEtter16Aar = 1,
                epsHarPensjon = true,
                epsHarInntektOver2G = false,
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        aarligBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1), // skal forbli 2025-01-01
                    ),
                    InntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 8, 9), // skal bli 2027-09-01
                    )
                ),
                rettTilAfpOffentligDato = LocalDate.of(2019, 10, 11) // skal forbli 2019-10-11 (siden ikke uttak/inntekt)
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
                        fom = LocalDate.of(2027, 9, 1),
                    )
                ),
                rettTilAfpOffentligDato = LocalDate.of(2019, 10, 11)

            )
        )
    }
})
