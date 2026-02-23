package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import java.time.LocalDate

class OpptjeningsperiodeUtilTest : ShouldSpec({

    fun tpOrdning(tpNr: String = "1234") =
        TpOrdning(navn = "Ordning $tpNr", tpNr = tpNr, tssId = "tss-$tpNr")

    fun stillingsprosent(
        datoFom: LocalDate = LocalDate.of(2020, 1, 1),
        datoTom: LocalDate? = LocalDate.of(2025, 12, 31),
        prosent: Double = 100.0,
        aldersgrense: Int = 67,
        faktiskHovedloenn: String? = "500000",
        stillingsuavhengigTilleggsloenn: String? = "20000"
    ) = Stillingsprosent(
        datoFom = datoFom,
        datoTom = datoTom,
        stillingsprosent = prosent,
        aldersgrense = aldersgrense,
        faktiskHovedlonn = faktiskHovedloenn,
        stillingsuavhengigTilleggslonn = stillingsuavhengigTilleggsloenn
    )

    should("wrap mapped perioder in response with correct key") {
        val ordning = tpOrdning(tpNr = "5678")

        val response = OpptjeningsperiodeUtil.getOpptjeningsperiodeListe(
            tpOrdning = ordning,
            stillingsprosentListe = listOf(stillingsprosent())
        )

        with(response) {
            keys shouldHaveSize 1
            keys.first() shouldBe ordning
            this[ordning]!! shouldHaveSize 1
        }
    }

    should("return empty perioder when stillingsprosentListe is empty") {
        val ordning = tpOrdning()

        OpptjeningsperiodeUtil.getOpptjeningsperiodeListe(
            tpOrdning = ordning,
            stillingsprosentListe = emptyList()
        )[ordning]!!.shouldBeEmpty()
    }
})
