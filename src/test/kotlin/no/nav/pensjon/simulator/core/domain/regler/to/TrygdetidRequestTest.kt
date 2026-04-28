package no.nav.pensjon.simulator.core.domain.regler.to

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import java.time.LocalDate

class TrygdetidRequestTest : FunSpec({

    test("'set uttaksgradListe' should sort list in reverse start date order") {
        val uttaksgradListe = TrygdetidRequest().apply {
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    uttaksgrad = 50
                    fomDatoLd = LocalDate.of(2022, 1, 1) // second
                },
                Uttaksgrad().apply {
                    uttaksgrad = 20
                    fomDatoLd = null // first
                },
                Uttaksgrad().apply {
                    uttaksgrad = 80
                    fomDatoLd = LocalDate.of(2023, 1, 1) // third
                },
                Uttaksgrad().apply {
                    uttaksgrad = 40
                    fomDatoLd = LocalDate.of(2024, 1, 1) // fourth
                }
            )
        }.uttaksgradListe

        uttaksgradListe[0].uttaksgrad shouldBe 40 // fourth
        uttaksgradListe[1].uttaksgrad shouldBe 80 // third
        uttaksgradListe[2].uttaksgrad shouldBe 50 // second
        uttaksgradListe[3].uttaksgrad shouldBe 20 // first
    }
})
