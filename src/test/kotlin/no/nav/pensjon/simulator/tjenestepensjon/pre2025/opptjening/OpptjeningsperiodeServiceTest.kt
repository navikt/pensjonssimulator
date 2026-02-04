package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.error.DuplicateOpptjeningsperiodeEndDateException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.error.MissingOpptjeningsperiodeException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import java.time.LocalDate

class OpptjeningsperiodeServiceTest : FunSpec({

    val service = OpptjeningsperiodeService()

    fun tpOrdning(tpNr: String = "1234") = TpOrdningFullDto(navn = "Ordning $tpNr", tpNr = tpNr, tssId = "tss-$tpNr")

    fun stillingsprosent(
        datoFom: LocalDate = LocalDate.of(2020, 1, 1),
        datoTom: LocalDate? = LocalDate.of(2025, 12, 31),
        prosent: Double = 100.0,
        aldersgrense: Int = 67,
        faktiskHovedlonn: String? = "500000",
        stillingsuavhengigTilleggslonn: String? = "20000"
    ) = Stillingsprosent(
        datoFom = datoFom,
        datoTom = datoTom,
        stillingsprosent = prosent,
        aldersgrense = aldersgrense,
        faktiskHovedlonn = faktiskHovedlonn,
        stillingsuavhengigTilleggslonn = stillingsuavhengigTilleggslonn,
        utvidelse = null
    )

    fun opptjeningsperiode(
        datoFom: LocalDate = LocalDate.of(2020, 1, 1),
        datoTom: LocalDate? = LocalDate.of(2025, 12, 31),
        prosent: Double = 100.0,
        aldersgrense: Int? = 67,
        faktiskHovedlonn: Int? = 500000,
        stillingsuavhengigTilleggslonn: Int? = 20000
    ) = OpptjeningsperiodeDto(
        datoFom = datoFom,
        datoTom = datoTom,
        stillingsprosent = prosent,
        aldersgrense = aldersgrense,
        faktiskHovedlonn = faktiskHovedlonn,
        stillingsuavhengigTilleggslonn = stillingsuavhengigTilleggslonn
    )

    // --- mapStillingsprosentToOpptjeningsperiodeList ---

    test("mapStillingsprosentToOpptjeningsperiodeList maps all fields correctly") {
        val input = listOf(
            stillingsprosent(
                datoFom = LocalDate.of(2020, 1, 1),
                datoTom = LocalDate.of(2025, 12, 31),
                prosent = 80.0,
                aldersgrense = 70,
                faktiskHovedlonn = "600000",
                stillingsuavhengigTilleggslonn = "30000"
            )
        )

        val result = service.mapStillingsprosentToOpptjeningsperiodeList(input)

        result shouldHaveSize 1
        result[0].datoFom shouldBe LocalDate.of(2020, 1, 1)
        result[0].datoTom shouldBe LocalDate.of(2025, 12, 31)
        result[0].stillingsprosent shouldBe 80.0
        result[0].aldersgrense shouldBe 70
        result[0].faktiskHovedlonn shouldBe 600000
        result[0].stillingsuavhengigTilleggslonn shouldBe 30000
    }

    test("mapStillingsprosentToOpptjeningsperiodeList converts null lonn strings to null ints") {
        val input = listOf(
            stillingsprosent(faktiskHovedlonn = null, stillingsuavhengigTilleggslonn = null)
        )

        val result = service.mapStillingsprosentToOpptjeningsperiodeList(input)

        result[0].faktiskHovedlonn shouldBe null
        result[0].stillingsuavhengigTilleggslonn shouldBe null
    }

    test("mapStillingsprosentToOpptjeningsperiodeList converts non-numeric strings to null") {
        val input = listOf(
            stillingsprosent(faktiskHovedlonn = "abc", stillingsuavhengigTilleggslonn = "xyz")
        )

        val result = service.mapStillingsprosentToOpptjeningsperiodeList(input)

        result[0].faktiskHovedlonn shouldBe null
        result[0].stillingsuavhengigTilleggslonn shouldBe null
    }

    test("mapStillingsprosentToOpptjeningsperiodeList handles null datoTom") {
        val input = listOf(stillingsprosent(datoTom = null))

        val result = service.mapStillingsprosentToOpptjeningsperiodeList(input)

        result[0].datoTom shouldBe null
    }

    test("mapStillingsprosentToOpptjeningsperiodeList returns empty list for empty input") {
        val result = service.mapStillingsprosentToOpptjeningsperiodeList(emptyList())

        result.shouldBeEmpty()
    }

    test("mapStillingsprosentToOpptjeningsperiodeList maps multiple entries") {
        val input = listOf(
            stillingsprosent(datoFom = LocalDate.of(2020, 1, 1), datoTom = LocalDate.of(2022, 12, 31)),
            stillingsprosent(datoFom = LocalDate.of(2023, 1, 1), datoTom = null)
        )

        val result = service.mapStillingsprosentToOpptjeningsperiodeList(input)

        result shouldHaveSize 2
        result[0].datoFom shouldBe LocalDate.of(2020, 1, 1)
        result[1].datoFom shouldBe LocalDate.of(2023, 1, 1)
    }

    // --- getOpptjeningsperiodeListe ---

    test("getOpptjeningsperiodeListe wraps mapped perioder in response with correct key") {
        val ordning = tpOrdning("5678")
        val input = listOf(stillingsprosent())

        val response = service.getOpptjeningsperiodeListe(ordning, input)

        response.tpOrdningOpptjeningsperiodeMap.keys shouldHaveSize 1
        response.tpOrdningOpptjeningsperiodeMap.keys.first() shouldBe ordning
        response.tpOrdningOpptjeningsperiodeMap[ordning]!! shouldHaveSize 1
    }

    test("getOpptjeningsperiodeListe returns empty exceptions list") {
        val response = service.getOpptjeningsperiodeListe(tpOrdning(), listOf(stillingsprosent()))

        response.exceptions.shouldBeEmpty()
    }

    test("getOpptjeningsperiodeListe returns empty perioder when stillingsprosentListe is empty") {
        val ordning = tpOrdning()
        val response = service.getOpptjeningsperiodeListe(ordning, emptyList())

        response.tpOrdningOpptjeningsperiodeMap[ordning]!!.shouldBeEmpty()
    }

    // --- getLatestFromOpptjeningsperiode ---

    test("getLatestFromOpptjeningsperiode returns ordning with latest datoTom") {
        val ordning1 = tpOrdning("1111")
        val ordning2 = tpOrdning("2222")
        val map = mapOf(
            ordning1 to listOf(opptjeningsperiode(datoTom = LocalDate.of(2024, 12, 31))),
            ordning2 to listOf(opptjeningsperiode(datoTom = LocalDate.of(2025, 12, 31)))
        )

        val result = service.getLatestFromOpptjeningsperiode(map)

        result shouldBe ordning2
    }

    test("getLatestFromOpptjeningsperiode returns ordning with null datoTom as latest") {
        val ordning1 = tpOrdning("1111")
        val ordning2 = tpOrdning("2222")
        val map = mapOf(
            ordning1 to listOf(opptjeningsperiode(datoTom = LocalDate.of(2030, 12, 31))),
            ordning2 to listOf(opptjeningsperiode(datoTom = null))
        )

        val result = service.getLatestFromOpptjeningsperiode(map)

        result shouldBe ordning2
    }

    test("getLatestFromOpptjeningsperiode returns single entry") {
        val ordning = tpOrdning()
        val map = mapOf(ordning to listOf(opptjeningsperiode()))

        val result = service.getLatestFromOpptjeningsperiode(map)

        result shouldBe ordning
    }

    test("getLatestFromOpptjeningsperiode selects latest across multiple perioder in same ordning") {
        val ordning = tpOrdning()
        val map = mapOf(
            ordning to listOf(
                opptjeningsperiode(datoTom = LocalDate.of(2022, 12, 31)),
                opptjeningsperiode(datoTom = LocalDate.of(2025, 6, 30))
            )
        )

        val result = service.getLatestFromOpptjeningsperiode(map)

        result shouldBe ordning
    }

    test("getLatestFromOpptjeningsperiode throws MissingOpptjeningsperiodeException on empty map") {
        shouldThrow<MissingOpptjeningsperiodeException> {
            service.getLatestFromOpptjeningsperiode(emptyMap())
        }
    }

    test("getLatestFromOpptjeningsperiode throws MissingOpptjeningsperiodeException when all lists are empty") {
        val map = mapOf(tpOrdning() to emptyList<OpptjeningsperiodeDto>())

        shouldThrow<MissingOpptjeningsperiodeException> {
            service.getLatestFromOpptjeningsperiode(map)
        }
    }

    test("getLatestFromOpptjeningsperiode throws DuplicateOpptjeningsperiodeEndDateException on same datoTom") {
        val ordning = tpOrdning()
        val sameDato = LocalDate.of(2025, 12, 31)
        val map = mapOf(
            ordning to listOf(
                opptjeningsperiode(datoTom = sameDato),
                opptjeningsperiode(datoTom = sameDato)
            )
        )

        shouldThrow<DuplicateOpptjeningsperiodeEndDateException> {
            service.getLatestFromOpptjeningsperiode(map)
        }
    }

    test("getLatestFromOpptjeningsperiode does not throw when both datoTom are null") {
        val ordning1 = tpOrdning("1111")
        val ordning2 = tpOrdning("2222")
        val map = mapOf(
            ordning1 to listOf(opptjeningsperiode(datoTom = null)),
            ordning2 to listOf(opptjeningsperiode(datoTom = null))
        )

        // null?.equals(null) returns null, so the duplicate check is not triggered
        val result = service.getLatestFromOpptjeningsperiode(map)

        // The second null-datoTom entry wins via reduce
        result shouldBe ordning2
    }

    test("getLatestFromOpptjeningsperiode prefers earlier entry when its datoTom is later") {
        val ordning1 = tpOrdning("1111")
        val ordning2 = tpOrdning("2222")
        val map = mapOf(
            ordning1 to listOf(opptjeningsperiode(datoTom = LocalDate.of(2026, 6, 30))),
            ordning2 to listOf(opptjeningsperiode(datoTom = LocalDate.of(2024, 12, 31)))
        )

        val result = service.getLatestFromOpptjeningsperiode(map)

        result shouldBe ordning1
    }
})
