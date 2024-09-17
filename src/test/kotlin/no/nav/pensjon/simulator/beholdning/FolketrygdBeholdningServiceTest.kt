package no.nav.pensjon.simulator.beholdning

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
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
                        inntektFom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 8, 1),
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
                uttakFom = LocalDate.of(2030, 2, 1),
                antallAarUtenlandsEtter16Aar = 1,
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 8, 1),
                    )
                ),
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )
    }

    test("simulerFolketrygdBeholdning gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 1, 2),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden"
    }

    test("simulerFolketrygdBeholdning gir feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2025, 1, 1), // samme fom som forrige
                    )
                )
            )
        }

        exception.message shouldBe "To fremtidige inntekter har samme f.o.m.-dato"
    }

    test("simulerFolketrygdBeholdning gir feilmelding for negativ inntekt") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = -1,
                        inntektFom = LocalDate.of(2027, 1, 1),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har negativt beløp"
    }
})

private fun simulerFolketrygdBeholdning(inntektSpecListe: List<InntektSpec>): FolketrygdBeholdning {
    val client = mock(BeholdningClient::class.java)

    return FolketrygdBeholdningService(client).simulerFolketrygdBeholdning(
        FolketrygdBeholdningSpec(
            pid = Pid("12906498357"),
            uttakFom = LocalDate.of(2031, 1, 1),
            fremtidigInntektListe = inntektSpecListe,
            antallAarUtenlandsEtter16Aar = 0,
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )
    )
}
