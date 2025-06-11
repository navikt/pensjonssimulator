package no.nav.pensjon.simulator.core.endring

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate
import java.util.*

class EndringUttaksgradTest : FunSpec({

    test("uttaksgradListe should return 2 items when gradert uttak") {
        val uttaksgradListe = EndringUttaksgrad(
            kravService = mockk(relaxed = true),
        ).uttaksgradListe(
            // Default values in spec:
            // - uttaksgrad: 50 %
            // - heltUttakDato: 2032-06-01
            spec = simuleringSpec(foersteUttakDato = LocalDate.of(2028, 2, 1)),
            forrigeAlderspensjonKravhodeId = 1L
        )

        uttaksgradListe.size shouldBe 2
        with(uttaksgradListe[0]) {
            uttaksgrad shouldBe 50
            fomDato shouldBe dateAtNoon(2028, Calendar.FEBRUARY, 1) // foersteUttakDato
            tomDato shouldBe dateAtNoon(2032, Calendar.MAY, 31) // heltUttakDato minus 1 dag
        }
        with(uttaksgradListe[1]) {
            uttaksgrad shouldBe 100
            fomDato shouldBe dateAtNoon(2032, Calendar.JUNE, 1) // heltUttakDato
            tomDato shouldBe null
        }
    }
})
