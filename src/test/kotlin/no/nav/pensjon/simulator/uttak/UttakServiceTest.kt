package no.nav.pensjon.simulator.uttak

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import org.mockito.Mockito.mock
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
        simuleringFacade = mock(SimuleringFacade::class.java),
        normAlderService = mock(NormAlderService::class.java)
    ).finnTidligstMuligUttak(
        simuleringSpec(inntektSpecListe)
    )
}
