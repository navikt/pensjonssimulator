package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode as InternalFeilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode as InternalYtelseCode
import java.time.LocalDate

class SimulerOffentligTjenestepensjonResultMapperV1Test : FunSpec({

    test("toDto maps all top-level fields correctly") {
        val source = result(
            tpnr = "3010",
            navnOrdning = "Statens pensjonskasse",
            inkluderteOrdningerListe = listOf("3010", "3020"),
            leverandorUrl = "https://spk.no",
            brukerErIkkeMedlemAvTPOrdning = false,
            brukerErMedlemAvTPOrdningSomIkkeStoettes = true,
            relevanteTpOrdninger = listOf("3010")
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.tpnr shouldBe "3010"
        dto.navnOrdning shouldBe "Statens pensjonskasse"
        dto.inkluderteOrdningerListe shouldBe listOf("3010", "3020")
        dto.leverandorUrl shouldBe "https://spk.no"
        dto.brukerErIkkeMedlemAvTPOrdning shouldBe false
        dto.brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
        dto.relevanteTpOrdninger shouldBe listOf("3010")
    }

    test("toDto maps utbetalingsperiode fields") {
        val source = result(
            utbetalingsperiodeListe = listOf(
                utbetalingsperiode(
                    uttaksgrad = 80,
                    arligUtbetaling = 250000.0,
                    datoFom = LocalDate.of(2030, 1, 1),
                    datoTom = LocalDate.of(2040, 12, 31)
                )
            )
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe.size shouldBe 1
        val periode = dto.utbetalingsperiodeListe.first()!!
        periode.uttaksgrad shouldBe 80
        periode.arligUtbetaling shouldBe 250000.0
        periode.datoFom shouldBe LocalDate.of(2030, 1, 1)
        periode.datoTom shouldBe LocalDate.of(2040, 12, 31)
    }

    test("toDto maps YtelseCode AP correctly") {
        val source = result(
            utbetalingsperiodeListe = listOf(
                utbetalingsperiode(ytelsekode = InternalYtelseCode.AP)
            )
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe.first()!!.ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
    }

    test("toDto maps YtelseCode AFP correctly") {
        val source = result(
            utbetalingsperiodeListe = listOf(
                utbetalingsperiode(ytelsekode = InternalYtelseCode.AFP)
            )
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe.first()!!.ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AFP
    }

    test("toDto maps YtelseCode SERALDER correctly") {
        val source = result(
            utbetalingsperiodeListe = listOf(
                utbetalingsperiode(ytelsekode = InternalYtelseCode.SERALDER)
            )
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe.first()!!.ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.SERALDER
    }

    test("toDto maps null ytelsekode to null") {
        val source = result(
            utbetalingsperiodeListe = listOf(
                utbetalingsperiode(ytelsekode = null)
            )
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe.first()!!.ytelsekode shouldBe null
    }

    test("toDto maps null datoTom to null") {
        val source = result(
            utbetalingsperiodeListe = listOf(
                utbetalingsperiode(datoTom = null)
            )
        )

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe.first()!!.datoTom shouldBe null
    }

    test("toDto maps feilkode TEKNISK_FEIL") {
        val source = result(feilkode = InternalFeilkode.TEKNISK_FEIL)

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.feilkode shouldBe Feilkode.TEKNISK_FEIL
    }

    test("toDto maps feilkode BEREGNING_GIR_NULL_UTBETALING") {
        val source = result(feilkode = InternalFeilkode.BEREGNING_GIR_NULL_UTBETALING)

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.feilkode shouldBe Feilkode.BEREGNING_GIR_NULL_UTBETALING
    }

    test("toDto maps null feilkode to null") {
        val source = result(feilkode = null)

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.feilkode shouldBe null
    }

    test("toDto maps empty utbetalingsperiodeListe") {
        val source = result(utbetalingsperiodeListe = emptyList())

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.utbetalingsperiodeListe shouldBe emptyList()
    }

    test("toDto maps brukerErIkkeMedlemAvTPOrdning = true") {
        val source = result(brukerErIkkeMedlemAvTPOrdning = true)

        val dto = SimulerOffentligTjenestepensjonResultMapperV1.toDto(source)

        dto.brukerErIkkeMedlemAvTPOrdning shouldBe true
    }
})

private fun result(
    tpnr: String = "3010",
    navnOrdning: String = "Ordning",
    inkluderteOrdningerListe: List<String> = emptyList(),
    leverandorUrl: String? = null,
    utbetalingsperiodeListe: List<Utbetalingsperiode> = emptyList(),
    brukerErIkkeMedlemAvTPOrdning: Boolean = false,
    brukerErMedlemAvTPOrdningSomIkkeStoettes: Boolean = false,
    feilkode: InternalFeilkode? = null,
    relevanteTpOrdninger: List<String> = emptyList()
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

private fun utbetalingsperiode(
    uttaksgrad: Int = 100,
    arligUtbetaling: Double = 200000.0,
    datoFom: LocalDate = LocalDate.of(2030, 1, 1),
    datoTom: LocalDate? = LocalDate.of(2040, 12, 31),
    ytelsekode: InternalYtelseCode? = InternalYtelseCode.AP
) = Utbetalingsperiode(
    uttaksgrad = uttaksgrad,
    arligUtbetaling = arligUtbetaling,
    datoFom = datoFom,
    datoTom = datoTom,
    ytelsekode = ytelsekode
)
