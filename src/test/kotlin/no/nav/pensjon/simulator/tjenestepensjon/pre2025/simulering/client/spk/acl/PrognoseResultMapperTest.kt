package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import java.time.LocalDate

class PrognoseResultMapperTest : FunSpec({

    test("fromDto maps tpnr and navnOrdning") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            inkluderteOrdningerListe = emptyList(),
            utbetalingsperiodeListe = emptyList()
        )

        val result = PrognoseResultMapper.fromDto(response)

        with(result) {
            tpnr shouldBe "3010"
            navnOrdning shouldBe "SPK"
        }
    }

    test("fromDto maps inkluderteOrdningerListe") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            inkluderteOrdningerListe = listOf("3010", "3020")
        )

        PrognoseResultMapper.fromDto(response).inkluderteOrdningerListe shouldBe listOf("3010", "3020")
    }

    test("fromDto maps leverandorUrl") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            leverandorUrl = "https://spk.no"
        )

        PrognoseResultMapper.fromDto(response).leverandorUrl shouldBe "https://spk.no"
    }

    test("fromDto maps utbetalingsperiode fields") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                UtbetalingsperiodeDto(
                    uttaksgrad = 100,
                    arligUtbetaling = 250000.0,
                    datoFom = LocalDate.of(2030, 1, 1),
                    datoTom = LocalDate.of(2035, 12, 31),
                    ytelsekode = "AP"
                )
            )
        )

        val result = PrognoseResultMapper.fromDto(response)

        with(result) {
            utbetalingsperiodeListe shouldHaveSize 1
            with(utbetalingsperiodeListe[0]) {
                uttaksgrad shouldBe 100
                arligUtbetaling shouldBe 250000.0
                datoFom shouldBe LocalDate.of(2030, 1, 1)
                datoTom shouldBe LocalDate.of(2035, 12, 31)
                ytelsekode shouldBe YtelseCode.AP
            }
        }
    }

    test("fromDto maps ytelsekode AFP") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                UtbetalingsperiodeDto(
                    uttaksgrad = 50, arligUtbetaling = 100000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "AFP"
                )
            )
        )

        PrognoseResultMapper.fromDto(response).utbetalingsperiodeListe[0].ytelsekode shouldBe YtelseCode.AFP
    }

    test("fromDto maps ytelsekode SERALDER") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                UtbetalingsperiodeDto(
                    uttaksgrad = 100, arligUtbetaling = 300000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "SERALDER"
                )
            )
        )

        PrognoseResultMapper.fromDto(response).utbetalingsperiodeListe[0].ytelsekode shouldBe YtelseCode.SERALDER
    }

    test("fromDto maps null datoTom") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                UtbetalingsperiodeDto(
                    uttaksgrad = 100, arligUtbetaling = 200000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "AP"
                )
            )
        )

        PrognoseResultMapper.fromDto(response).utbetalingsperiodeListe[0].datoTom shouldBe null
    }

    test("fromDto filters out null entries in utbetalingsperiodeListe") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                null,
                UtbetalingsperiodeDto(
                    uttaksgrad = 100, arligUtbetaling = 200000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "AP"
                ),
                null
            )
        )

        PrognoseResultMapper.fromDto(response).utbetalingsperiodeListe shouldHaveSize 1
    }

    test("fromDto maps empty utbetalingsperiodeListe") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = emptyList()
        )

        PrognoseResultMapper.fromDto(response).utbetalingsperiodeListe.shouldBeEmpty()
    }

    test("fromDto maps brukerErIkkeMedlemAvTPOrdning") {
        val response = HentPrognoseResponseDto(
            tpnr = "",
            navnOrdning = "",
            brukerErIkkeMedlemAvTPOrdning = true
        )

        PrognoseResultMapper.fromDto(response).brukerErIkkeMedlemAvTPOrdning shouldBe true
    }

    test("fromDto maps brukerErMedlemAvTPOrdningSomIkkeStoettes") {
        val response = HentPrognoseResponseDto(
            tpnr = "",
            navnOrdning = "",
            brukerErMedlemAvTPOrdningSomIkkeStoettes = true
        )

        PrognoseResultMapper.fromDto(response).brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
    }

    test("fromDto defaults membership booleans to false") {
        val response = HentPrognoseResponseDto(tpnr = "3010", navnOrdning = "SPK")

        val result = PrognoseResultMapper.fromDto(response)

        with(result) {
            brukerErIkkeMedlemAvTPOrdning shouldBe false
            brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe false
        }
    }
})
