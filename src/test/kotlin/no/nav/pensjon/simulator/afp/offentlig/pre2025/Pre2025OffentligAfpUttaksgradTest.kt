package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate
import java.util.*

class Pre2025OffentligAfpUttaksgradTest : FunSpec({

    /**
     * Tester at uttaksgrad-listen ved førstegangsuttak inneholder 100 % grad f.o.m. 1. dag i måneden etter at
     * søker oppnår normert pensjonalser. Uttaksgraden skal være tidsubegrenset.
     * --------------
     * Med fødselsdato 1963-01-15 og normalder 67 år, så oppnås normalder 2030-01-15.
     * 1. dag i måneden etter dette er 2030-02-01.
     */
    test("uttaksgradListe ved førstegangsuttak skal inneholde 100 % uttaksgrad") {
        val uttaksgradListe = Pre2025OffentligAfpUttaksgrad(
            kravService = mockk(),
            normalderService = Arrange.normalder(foedselsdato = LocalDate.of(1963, 1, 15))
        ).uttaksgradListe(
            spec = simuleringSpec,
            forrigeAlderspensjonBeregningResultat = null, // => førstegangsuttak
            foedselsdato = LocalDate.of(1963, 1, 15)
        )

        uttaksgradListe.size shouldBe 1
        with(uttaksgradListe[0]) {
            fomDato shouldBe dateAtNoon(2030, Calendar.FEBRUARY, 1)
            tomDato shouldBe null // tidsubegrenset
            uttaksgrad shouldBe 100
        }
    }
})
