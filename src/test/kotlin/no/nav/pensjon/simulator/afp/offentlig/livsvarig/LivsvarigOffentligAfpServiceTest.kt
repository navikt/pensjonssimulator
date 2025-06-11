package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.LivsvarigOffentligAfpClient
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class LivsvarigOffentligAfpServiceTest : FunSpec({

    val client = mockk<LivsvarigOffentligAfpClient>(relaxed = true)
    val foedselsdato = LocalDate.of(1964, 1, 2)
    val virkningDato = LocalDate.of(2026, 1, 1)
    val today = LocalDate.of(2025, 1, 15)

    test("beregnAfp med for ung person => resultat er 'null'") {
        LivsvarigOffentligAfpService(client) { today }.beregnAfp(
            pid,
            foedselsdato = LocalDate.of(1962, 12, 31),
            forventetAarligInntektBeloep = 0,
            fremtidigeInntekter = emptyList(),
            brukFremtidigInntekt = false,
            virkningDato
        ) shouldBe null

        verify { client wasNot Called }
    }

    test("beregnAfp med for tidlig virkning => resultat er 'null'") {
        LivsvarigOffentligAfpService(client) { today }.beregnAfp(
            pid,
            foedselsdato = LocalDate.of(1970, 1, 1),
            forventetAarligInntektBeloep = 0,
            fremtidigeInntekter = emptyList(),
            brukFremtidigInntekt = false,
            virkningDato = LocalDate.of(2031, 12, 31)
        ) shouldBe null

        verify { client wasNot Called }
    }

    test("beregnAfp uten inntektliste => simuler med inntekter basert på forventet inntekt og alder") {
        LivsvarigOffentligAfpService(client) { today }.beregnAfp(
            pid,
            foedselsdato,
            forventetAarligInntektBeloep = 123000,
            fremtidigeInntekter = emptyList(),
            brukFremtidigInntekt = false,
            virkningDato
        )

        verify(exactly = 1) {
            client.simuler(
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
    }

    test("beregnAfp med tom inntektliste => simuler uten inntekter") {
        LivsvarigOffentligAfpService(client) { today }.beregnAfp(
            pid,
            foedselsdato,
            forventetAarligInntektBeloep = 123000,
            fremtidigeInntekter = emptyList(),
            brukFremtidigInntekt = true,
            virkningDato
        )

        verify(exactly = 1) {
            client.simuler(
                LivsvarigOffentligAfpSpec(
                    pid,
                    foedselsdato,
                    fom = virkningDato,
                    fremtidigInntektListe = emptyList()
                )
            )
        }
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
            brukFremtidigInntekt = true,
            virkningDato
        )

        verify(exactly = 1) {
            client.simuler(
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
    }
})
