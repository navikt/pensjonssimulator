package no.nav.pensjon.simulator.core.domain.regler.to

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class TrygdetidRequestTest : FunSpec({

    test("'set uttaksgradListe' should sort list in reverse start date order") {
        val uttaksgradListe = TrygdetidRequest().apply {
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    uttaksgrad = 50
                    fomDato = dateAtNoon(2022, Calendar.JANUARY, 1) // second
                },
                Uttaksgrad().apply {
                    uttaksgrad = 20
                    fomDato = null // first
                },
                Uttaksgrad().apply {
                    uttaksgrad = 80
                    fomDato = dateAtNoon(2023, Calendar.JANUARY, 1) // third
                },
                Uttaksgrad().apply {
                    uttaksgrad = 40
                    fomDato = dateAtNoon(2024, Calendar.JANUARY, 1) // fourth
                }
            )
        }.uttaksgradListe

        uttaksgradListe[0].uttaksgrad shouldBe 40 // fourth
        uttaksgradListe[1].uttaksgrad shouldBe 80 // third
        uttaksgradListe[2].uttaksgrad shouldBe 50 // second
        uttaksgradListe[3].uttaksgrad shouldBe 20 // first
    }
})
