package no.nav.pensjon.simulator.alderspensjon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import java.time.LocalDate

class AlderspensjonSpecTest : FunSpec({

    test("validated returnerer seg selv når verdier er OK") {
        val spec = alderspensjonSpec(
            heltUttakFom = LocalDate.of(2031, 1, 1) // etter gradert uttak (OK)
        )

        spec.validated() shouldBe spec
    }

    test("validated gir feilmelding når helt uttak ikke starter etter gradert uttak") {
        val exception = shouldThrow<BadRequestException> {
            alderspensjonSpec(
                heltUttakFom = LocalDate.of(2030, 1, 1) // ikke etter gradert uttak
            ).validated()
        }

        exception.message shouldBe "Helt uttak starter ikke etter gradert uttak"
    }
})

private fun alderspensjonSpec(heltUttakFom: LocalDate) =
    AlderspensjonSpec(
        pid = Pid("12906498357"),
        gradertUttak = GradertUttakSpec(
            uttaksgrad = Uttaksgrad.FEMTI_PROSENT,
            fom = LocalDate.of(2030, 1, 1)
        ),
        heltUttakFom,
        antallAarUtenlandsEtter16Aar = 0,
        epsHarPensjon = false,
        epsHarInntektOver2G = false,
        fremtidigInntektListe = emptyList(),
        rettTilAfpOffentligDato = null
    )
