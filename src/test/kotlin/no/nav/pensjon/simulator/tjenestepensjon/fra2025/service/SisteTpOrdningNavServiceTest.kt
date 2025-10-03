package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning.SisteTpOrdningNavService
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import java.time.LocalDate

class SisteTpOrdningNavServiceTest : FunSpec({
    val sisteTpOrdningNavService = SisteTpOrdningNavService()

    test("finn siste ordning fra tom liste returnerer tom liste") {
        sisteTpOrdningNavService.finnSisteOrdningKandidater(emptyList()) shouldBe emptyList()
    }

    test("finn siste ordning sorterer med siste dato foerst") {
        val tpOrdninger = listOf(
            TpForhold(
                tpNr = "tpNr2",
                navn = "2",
                datoSistOpptjening = LocalDate.now().minusYears(2)
            ), TpForhold(
                tpNr = "tpNr1",
                navn = "1",
                datoSistOpptjening = LocalDate.now().minusYears(1),
            ), TpForhold(
                tpNr = "tpNr3",
                navn = "3",
                datoSistOpptjening = LocalDate.now().minusYears(3)
            )
        )
        sisteTpOrdningNavService.finnSisteOrdningKandidater(tpOrdninger) shouldBe listOf("tpNr1", "tpNr2", "tpNr3")
    }

    test("finn siste ordning returnerer tp-ordninger uten dato foerst") {
        val tpOrdninger = listOf(
            TpForhold(
                tpNr = "tpNr2",
                navn = "2",
                datoSistOpptjening = LocalDate.now().minusYears(2)
            ), TpForhold(
                tpNr = "tpNr1",
                navn = "1",
                datoSistOpptjening = LocalDate.now().minusYears(1),
            ), TpForhold(
                tpNr = "tpNr3",
                navn = "3",
                datoSistOpptjening = LocalDate.now().minusYears(3)
            ), TpForhold(
                tpNr = "tpNr4",
                navn = "4",
                datoSistOpptjening = null
            )
        )
        sisteTpOrdningNavService.finnSisteOrdningKandidater(tpOrdninger) shouldBe listOf("tpNr4", "tpNr1", "tpNr2", "tpNr3")
    }

    test("finn siste ordning tolererer flere tp-ordninger uten dato") {
        val tpOrdninger = listOf(
            TpForhold(
                tpNr = "tpNr4",
                navn = "4",
                datoSistOpptjening = null
            ), TpForhold(
                tpNr = "tpNr5",
                navn = "5",
                datoSistOpptjening = null
            )
        )
        sisteTpOrdningNavService.finnSisteOrdningKandidater(tpOrdninger) shouldBe listOf("tpNr4", "tpNr5")
    }

})
