package no.nav.pensjon.simulator.core.endring

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class EndringUttaksgradTest : ShouldSpec({

    should("gi 2 elementer når gradert uttak") {
        val uttaksgradListe = EndringUttaksgrad(
            kravService = mockk(relaxed = true),
        ).uttaksgradListe(
            // Default values in spec:
            // - uttaksgrad: 50 %
            // - heltUttakDato: 2032-06-01
            spec = simuleringSpec(foersteUttakDato = LocalDate.of(2028, 2, 1)).uttak(),
            forrigeAlderspensjonKravhodeId = 1L
        )

        uttaksgradListe.size shouldBe 2
        with(uttaksgradListe[0]) {
            uttaksgrad shouldBe 50
            fomDatoLd shouldBe LocalDate.of(2028, 2, 1) // foersteUttakDato
            tomDatoLd shouldBe LocalDate.of(2032, 5, 31) // heltUttakDato minus 1 dag
        }
        with(uttaksgradListe[1]) {
            uttaksgrad shouldBe 100
            fomDatoLd shouldBe LocalDate.of(2032, 6, 1) // heltUttakDato
            tomDatoLd shouldBe null
        }
    }
})