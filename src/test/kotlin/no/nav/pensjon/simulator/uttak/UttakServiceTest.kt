package no.nav.pensjon.simulator.uttak

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class UttakServiceTest : FunSpec({

    /* TODO
    test("finnTidligstMuligUttak gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            finnTidligstMuligUttak(
                listOf(
                    FremtidigInntekt(
                        aarligInntektBeloep = 10000,
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
                    FremtidigInntekt(
                        aarligInntektBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1),
                    ),
                    FremtidigInntekt(
                        aarligInntektBeloep = 10000,
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
                    FremtidigInntekt(
                        aarligInntektBeloep = -1,
                        fom = LocalDate.of(2027, 1, 1),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har negativt beløp"
    }
    */
})

private fun finnTidligstMuligUttak(inntektSpecListe: List<FremtidigInntekt>): TidligstMuligUttak {

    return UttakService(
        simuleringFacade = mockk(),
        normalderService = mockk(),
        time = { LocalDate.of(2021, 1, 1) }
    ).finnTidligstMuligUttak(
        simuleringSpec(inntektSpecListe = inntektSpecListe)
    )
}
