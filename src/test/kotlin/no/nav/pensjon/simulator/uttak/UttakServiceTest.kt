package no.nav.pensjon.simulator.uttak

import io.kotest.core.spec.style.FunSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.client.UttakClient
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate

class UttakServiceTest : FunSpec({

    test("finnTidligstMuligUttak bruker 1. i denne/neste måned for uttak og inntekt") {
        val client = mock(UttakClient::class.java)

        UttakService(client).finnTidligstMuligUttak(
            TidligstMuligUttakSpec(
                pid = Pid("12906498357"),
                foedselDato = LocalDate.of(1964, 10, 12), // skal forbli 1964-10-12
                gradertUttak = GradertUttakSpec(
                    grad = UttakGrad.AATTI_PROSENT,
                    heltUttakFom = LocalDate.of(2030, 1, 2), // skal bli 2030-02-01
                ),
                rettTilOffentligAfpFom = LocalDate.of(2019, 10, 11), // skal forbli 2019-10-11 (siden ikke uttak/inntekt)
                antallAarUtenlandsEtter16Aar = 1,
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
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )

        verify(client).finnTidligstMuligUttak(
            TidligstMuligUttakSpec(
                pid = Pid("12906498357"),
                foedselDato = LocalDate.of(1964, 10, 12),
                gradertUttak = GradertUttakSpec(
                    grad = UttakGrad.AATTI_PROSENT,
                    heltUttakFom = LocalDate.of(2030, 2, 1),
                ),
                rettTilOffentligAfpFom = LocalDate.of(2019, 10, 11),
                antallAarUtenlandsEtter16Aar = 1,
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
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )
    }
})
