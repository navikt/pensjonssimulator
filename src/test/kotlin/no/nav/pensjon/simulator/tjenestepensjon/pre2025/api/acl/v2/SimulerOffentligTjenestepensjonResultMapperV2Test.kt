package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import java.time.LocalDate

class SimulerOffentligTjenestepensjonResultMapperV2Test : FunSpec({

    test("toDto returns empty result when brukerErIkkeMedlemAvTPOrdning is true") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            resultIkkeMedlem()
        )

        result.simulertPensjonListe shouldBe null
    }

    test("toDto returns empty result when brukerErMedlemAvTPOrdningSomIkkeStoettes is true") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            resultOrdningStoettesIkke()
        )

        result.simulertPensjonListe shouldBe null
    }

    test("toDto maps tpnr and navnOrdning to simulertPensjon") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(tpnr = "3010", navnOrdning = "Statens pensjonskasse")
        )

        val simulertPensjon = result.simulertPensjonListe!!.first()
        simulertPensjon.tpnr shouldBe "3010"
        simulertPensjon.navnOrdning shouldBe "Statens pensjonskasse"
    }

    test("toDto maps inkluderteOrdninger from inkluderteOrdningerListe") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(inkluderteOrdningerListe = listOf("3010", "3020", "3030"))
        )

        val simulertPensjon = result.simulertPensjonListe!!.first()
        simulertPensjon.inkluderteOrdninger shouldBe listOf("3010", "3020", "3030")
    }

    test("toDto maps leverandorUrl") {
        val url = "https://example.com/leverandor"
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(leverandorUrl = url)
        )

        val simulertPensjon = result.simulertPensjonListe!!.first()
        simulertPensjon.leverandorUrl shouldBe url
    }

    test("toDto sets inkluderteTpnr, utelatteTpnr, status, feilkode, feilbeskrivelse to null") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult()
        )

        val simulertPensjon = result.simulertPensjonListe!!.first()
        simulertPensjon.inkluderteTpnr shouldBe null
        simulertPensjon.utelatteTpnr shouldBe null
        simulertPensjon.status shouldBe null
        simulertPensjon.feilkode shouldBe null
        simulertPensjon.feilbeskrivelse shouldBe null
    }

    test("toDto wraps simulertPensjon in a list of size 1") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult()
        )

        result.simulertPensjonListe!! shouldHaveSize 1
    }

    test("toDto maps utbetalingsperiode grad and arligUtbetaling") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(uttaksgrad = 80, arligUtbetaling = 250_000.0)
                )
            )
        )

        val periode = result.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.grad shouldBe 80
        periode.arligUtbetaling shouldBe 250_000.0
    }

    test("toDto converts datoFom to Norwegian Date") {
        val fom = LocalDate.of(2030, 7, 1)
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(datoFom = fom)
                )
            )
        )

        val periode = result.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.datoFom shouldBe fom.toNorwegianDate()
    }

    test("toDto maps null datoTom to null") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(datoTom = null)
                )
            )
        )

        val periode = result.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.datoTom shouldBe null
    }

    test("toDto maps ytelsekode as enum toString") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(ytelsekode = YtelseCode.AP),
                    utbetalingsperiode(ytelsekode = YtelseCode.AFP)
                )
            )
        )

        val perioder = result.simulertPensjonListe!!.first().utbetalingsperioder!!
        perioder shouldHaveSize 2
        perioder[0].ytelsekode shouldBe "AP"
        perioder[1].ytelsekode shouldBe "AFP"
    }

    test("toDto maps null ytelsekode to null") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(ytelsekode = null)
                )
            )
        )

        val periode = result.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.ytelsekode shouldBe null
    }

    test("toDto maps empty utbetalingsperiodeListe") {
        val result = SimulerOffentligTjenestepensjonResultMapperV2.toDto(
            domainResult(utbetalingsperiodeListe = emptyList())
        )

        val simulertPensjon = result.simulertPensjonListe!!.first()
        simulertPensjon.utbetalingsperioder!! shouldHaveSize 0
    }
})

private fun domainResult(
    tpnr: String = "3010",
    navnOrdning: String = "Statens pensjonskasse",
    inkluderteOrdningerListe: List<String> = emptyList(),
    leverandorUrl: String? = null,
    utbetalingsperiodeListe: List<Utbetalingsperiode> = emptyList()
) = SimulerOffentligTjenestepensjonResult(
    tpnr = tpnr,
    navnOrdning = navnOrdning,
    inkluderteOrdningerListe = inkluderteOrdningerListe,
    leverandorUrl = leverandorUrl,
    utbetalingsperiodeListe = utbetalingsperiodeListe,
    brukerErIkkeMedlemAvTPOrdning = false,
    brukerErMedlemAvTPOrdningSomIkkeStoettes = false
)

private fun resultIkkeMedlem() = SimulerOffentligTjenestepensjonResult(
    tpnr = "",
    navnOrdning = "",
    brukerErIkkeMedlemAvTPOrdning = true
)

private fun resultOrdningStoettesIkke() = SimulerOffentligTjenestepensjonResult(
    tpnr = "",
    navnOrdning = "",
    brukerErMedlemAvTPOrdningSomIkkeStoettes = true
)

private fun utbetalingsperiode(
    uttaksgrad: Int = 100,
    arligUtbetaling: Double = 200_000.0,
    datoFom: LocalDate = LocalDate.of(2030, 1, 1),
    datoTom: LocalDate? = null,
    ytelsekode: YtelseCode? = YtelseCode.AP
) = Utbetalingsperiode(
    uttaksgrad = uttaksgrad,
    arligUtbetaling = arligUtbetaling,
    datoFom = datoFom,
    datoTom = datoTom,
    ytelsekode = ytelsekode
)
