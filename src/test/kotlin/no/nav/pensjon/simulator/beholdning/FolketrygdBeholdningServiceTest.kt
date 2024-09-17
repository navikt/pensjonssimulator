package no.nav.pensjon.simulator.beholdning

import io.kotest.core.spec.style.FunSpec
import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import no.nav.pensjon.simulator.person.Pid
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate

class FolketrygdBeholdningServiceTest : FunSpec({

    test("simulerFolketrygdBeholdning bruker 1. i denne/neste måned for uttak og inntekt") {
        val client = mock(BeholdningClient::class.java)

        FolketrygdBeholdningService(client).simulerFolketrygdBeholdning(
            FolketrygdBeholdningSpec(
                pid = Pid("12906498357"),
                uttakFom = LocalDate.of(2030, 1, 2), // skal bli 2030-02-01
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1), // skal forbli 2025-01-01
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 8, 9), // skal bli 2027-09-01
                    )
                ),
                antallAarUtenlandsEtter16Aar = 1,
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )

        verify(client).simulerFolketrygdBeholdning(
            FolketrygdBeholdningSpec(
                pid = Pid("12906498357"),
                uttakFom = LocalDate.of(2030, 2, 1), // skal bli 2030-02-01
                antallAarUtenlandsEtter16Aar = 1,
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 9, 1),
                    )
                ),
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )
    }
})
