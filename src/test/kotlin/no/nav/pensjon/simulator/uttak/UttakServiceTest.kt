package no.nav.pensjon.simulator.uttak

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
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
                foedselDato = LocalDate.of(1964, 10, 12),
                gradertUttak = GradertUttakSpec(
                    grad = UttakGrad.AATTI_PROSENT,
                    heltUttakFom = LocalDate.of(2030, 1, 2), // skal bli 2030-02-01
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
                        fom = LocalDate.of(2027, 8, 1),
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
                        fom = LocalDate.of(2027, 8, 1),
                    )
                ),
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )
    }

    test("finnTidligstMuligUttak gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            finnTidligstMuligUttak(
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

    test("finnTidligstMuligUttak gir feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        val exception = shouldThrow<BadRequestException> {
            finnTidligstMuligUttak(
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

    test("finnTidligstMuligUttak gir feilmelding for negativ inntekt") {
        val exception = shouldThrow<BadRequestException> {
            finnTidligstMuligUttak(
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

private fun finnTidligstMuligUttak(inntektSpecListe: List<InntektSpec>): TidligstMuligUttak {
    val client = mock(UttakClient::class.java)

    return UttakService(client).finnTidligstMuligUttak(
        TidligstMuligUttakSpec(
            pid = Pid("12906498357"),
            foedselDato = LocalDate.of(1964, 10, 12),
            gradertUttak = null,
            rettTilOffentligAfpFom = null,
            antallAarUtenlandsEtter16Aar = 0,
            fremtidigInntektListe = inntektSpecListe,
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )
    )
}
