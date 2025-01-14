package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import io.kotest.core.spec.style.FunSpec
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.LivsvarigOffentligAfpClient
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.verify
import java.time.LocalDate

class LivsvarigOffentligAfpServiceTest : FunSpec({

    val client = mock(LivsvarigOffentligAfpClient::class.java)
    val foedselsdato = LocalDate.of(1964, 1, 2)
    val virkningDato = LocalDate.of(2026, 1, 1)
    val today = LocalDate.of(2025, 1, 15)

    test("beregnAfp uten inntektliste => simuler med inntekter basert på forventet inntekt og alder") {
        LivsvarigOffentligAfpService(client) { today }.beregnAfp(
            pid,
            foedselsdato,
            forventetAarligInntektBeloep = 123000,
            fremtidigeInntekter = emptyList(),
            virkningDato
        )

        verify(client, times(1)).simuler(
            LivsvarigOffentligAfpSpec(
                pid,
                foedselsdato,
                fom = virkningDato,
                fremtidigInntektListe = listOf(
                    Inntekt(aarligBeloep = 123000, LocalDate.of(2024, 1, 1)), // 2024 = 2025 - 1
                    Inntekt(aarligBeloep = 123000, LocalDate.of(2025, 1, 1))  // 2025 = 1964 + 62 - 1
                )
            )
        )
    }

    test("beregnAfp med inntektliste => simuler med gitte inntekter") {
        LivsvarigOffentligAfpService(client) { today }.beregnAfp(
            pid,
            foedselsdato,
            forventetAarligInntektBeloep = 0,
            fremtidigeInntekter = listOf(
                FremtidigInntekt(aarligInntektBeloep = 234000, fom = LocalDate.of(2024, 2, 1)),
                FremtidigInntekt(aarligInntektBeloep = 123000, fom = LocalDate.of(2025, 3, 1))
            ),
            virkningDato
        )

        verify(client, times(1)).simuler(
            LivsvarigOffentligAfpSpec(
                pid,
                foedselsdato,
                fom = virkningDato,
                fremtidigInntektListe = listOf(
                    Inntekt(aarligBeloep = 234000, LocalDate.of(2024, 2, 1)),
                    Inntekt(aarligBeloep = 123000, LocalDate.of(2025, 3, 1))
                )
            )
        )
    }
})
