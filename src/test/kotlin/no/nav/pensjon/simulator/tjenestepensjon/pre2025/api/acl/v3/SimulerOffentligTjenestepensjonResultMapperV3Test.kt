package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import java.time.LocalDate

class SimulerOffentligTjenestepensjonResultMapperV3Test : FunSpec({

    fun utbetalingsperiode(
        uttaksgrad: Int = 100,
        arligUtbetaling: Double = 250000.0,
        datoFom: LocalDate = LocalDate.of(2030, 1, 1),
        datoTom: LocalDate? = LocalDate.of(2040, 12, 31),
        ytelsekode: YtelseCode? = YtelseCode.AP
    ) = Utbetalingsperiode(
        uttaksgrad = uttaksgrad,
        arligUtbetaling = arligUtbetaling,
        datoFom = datoFom,
        datoTom = datoTom,
        ytelsekode = ytelsekode
    )

    fun result(
        tpnr: String = "3010",
        navnOrdning: String = "Statens pensjonskasse",
        inkluderteOrdningerListe: List<String> = listOf("3010", "3020"),
        leverandorUrl: String? = "https://spk.no",
        utbetalingsperiodeListe: List<Utbetalingsperiode> = listOf(utbetalingsperiode()),
        brukerErIkkeMedlemAvTPOrdning: Boolean = false,
        brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
        feilkode: Feilkode? = null,
        relevanteTpOrdninger: List<String> = listOf("3010")
    ) = SimulerOffentligTjenestepensjonResult(
        tpnr = tpnr,
        navnOrdning = navnOrdning,
        inkluderteOrdningerListe = inkluderteOrdningerListe,
        leverandorUrl = leverandorUrl,
        utbetalingsperiodeListe = utbetalingsperiodeListe,
        brukerErIkkeMedlemAvTPOrdning = brukerErIkkeMedlemAvTPOrdning,
        brukerErMedlemAvTPOrdningSomIkkeStoettes = brukerErMedlemAvTPOrdningSomIkkeStoettes,
        feilkode = feilkode,
        relevanteTpOrdninger = relevanteTpOrdninger
    )

    test("toDto returns BRUKER_IKKE_MEDLEM_AV_TP_ORDNING feilkode with empty simulertPensjonListe when brukerErIkkeMedlemAvTPOrdning is true") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(brukerErIkkeMedlemAvTPOrdning = true)
        )

        dto.feilkode shouldBe FeilkodeV3.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING
        dto.simulertPensjonListe shouldBe emptyList()
    }

    test("toDto returns TP_ORDNING_STOETTES_IKKE with null simulertPensjonListe and relevanteTpOrdninger when brukerErMedlemAvTPOrdningSomIkkeStoettes is true") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(
                brukerErMedlemAvTPOrdningSomIkkeStoettes = true,
                relevanteTpOrdninger = listOf("3010", "3020")
            )
        )

        dto.feilkode shouldBe FeilkodeV3.TP_ORDNING_STOETTES_IKKE
        dto.simulertPensjonListe shouldBe null
        dto.relevanteTpOrdninger shouldBe listOf("3010", "3020")
    }

    test("toDto maps tpnr and navnOrdning to simulertPensjon in happy path") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(tpnr = "4000", navnOrdning = "KLP")
        )

        val simulertPensjon = dto.simulertPensjonListe!!.first()
        simulertPensjon.tpnr shouldBe "4000"
        simulertPensjon.navnOrdning shouldBe "KLP"
    }

    test("toDto maps inkluderteOrdninger") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(inkluderteOrdningerListe = listOf("3010", "3020", "3030"))
        )

        val simulertPensjon = dto.simulertPensjonListe!!.first()
        simulertPensjon.inkluderteOrdninger shouldBe listOf("3010", "3020", "3030")
    }

    test("toDto maps leverandorUrl") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(leverandorUrl = "https://klp.no/simulering")
        )

        val simulertPensjon = dto.simulertPensjonListe!!.first()
        simulertPensjon.leverandorUrl shouldBe "https://klp.no/simulering"
    }

    test("toDto wraps simulertPensjon in a list of size 1") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(result())

        dto.simulertPensjonListe!! shouldHaveSize 1
    }

    test("toDto maps utbetalingsperiode grad and arligUtbetaling") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(uttaksgrad = 50, arligUtbetaling = 123456.78)
                )
            )
        )

        val periode = dto.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.grad shouldBe 50
        periode.arligUtbetaling shouldBe 123456.78
    }

    test("toDto converts datoFom to Norwegian Date") {
        val fom = LocalDate.of(2031, 6, 15)
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(datoFom = fom)
                )
            )
        )

        val periode = dto.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.datoFom shouldBe fom.toNorwegianDate()
    }

    test("toDto maps null datoTom to null") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(datoTom = null)
                )
            )
        )

        val periode = dto.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.datoTom shouldBe null
    }

    test("toDto maps ytelsekode as enum toString") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(ytelsekode = YtelseCode.AP)
                )
            )
        )

        val periode = dto.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.ytelsekode shouldBe "AP"
    }

    test("toDto maps null ytelsekode to null") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(
                utbetalingsperiodeListe = listOf(
                    utbetalingsperiode(ytelsekode = null)
                )
            )
        )

        val periode = dto.simulertPensjonListe!!.first().utbetalingsperioder!!.first()
        periode.ytelsekode shouldBe null
    }

    test("toDto maps feilkode using FeilkodeV3 externalValue") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(feilkode = Feilkode.TEKNISK_FEIL)
        )

        dto.feilkode shouldBe FeilkodeV3.TEKNISK_FEIL
    }

    test("toDto maps null feilkode to null") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(feilkode = null)
        )

        dto.feilkode shouldBe null
    }

    test("toDto passes relevanteTpOrdninger through in normal case") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(relevanteTpOrdninger = listOf("3010", "4000"))
        )

        dto.relevanteTpOrdninger shouldBe listOf("3010", "4000")
    }

    test("toDto maps empty utbetalingsperiodeListe") {
        val dto = SimulerOffentligTjenestepensjonResultMapperV3.toDto(
            result(utbetalingsperiodeListe = emptyList())
        )

        val simulertPensjon = dto.simulertPensjonListe!!.first()
        simulertPensjon.utbetalingsperioder shouldBe emptyList()
    }
})
