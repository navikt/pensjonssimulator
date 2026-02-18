package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.BrukerKvalifisererIkkeTilTjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SivilstandKode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SPKStillingsprosentService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import java.time.LocalDate

class TjenestepensjonSimuleringPre2025ServiceTest : FunSpec({

    val pid = Pid("12345678910")

    fun spec() = TjenestepensjonSimuleringPre2025Spec(
        pid = pid,
        foedselsdato = LocalDate.of(1963, 1, 15),
        sisteTpOrdningsTpNummer = "3010",
        simulertOffentligAfp = null,
        simulertPrivatAfp = null,
        sivilstand = SivilstandKode.UGIFT,
        inntekter = emptyList(),
        pensjonsbeholdningsperioder = emptyList(),
        simuleringsperioder = emptyList(),
        simuleringsdata = emptyList(),
        tpForhold = emptyList()
    )

    fun spkForhold(tpNr: String = "3010", navn: String = "SPK") =
        TpForhold(tpNr = tpNr, navn = navn, datoSistOpptjening = null)

    fun stillingsprosent() = Stillingsprosent(
        datoFom = LocalDate.of(2020, 1, 1),
        datoTom = null,
        stillingsprosent = 100.0,
        aldersgrense = 67,
        faktiskHovedlonn = "500000",
        stillingsuavhengigTilleggslonn = null,
        utvidelse = null
    )

    fun tpregisteretClient(
        forhold: List<TpForhold> = listOf(spkForhold()),
        tssIdMap: Map<String, String?> = mapOf("3010" to "tss-3010")
    ) = mockk<TpregisteretClient> {
        every { findAlleTpForhold(pid) } returns forhold
        tssIdMap.forEach { (tpNr, tssId) ->
            every { findTssId(tpNr) } returns tssId
        }
    }

    fun service(
        tpClient: TpregisteretClient = tpregisteretClient(),
        spkStillingsprosent: SPKStillingsprosentService = mockk {
            every { getStillingsprosentListe(pid.value, any()) } returns listOf(stillingsprosent())
        },
        spkTjenestepensjon: SPKTjenestepensjonServicePre2025 = mockk {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } returns SimulerOffentligTjenestepensjonResult(
                tpnr = "3010",
                navnOrdning = "SPK",
                utbetalingsperiodeListe = emptyList()
            )
        }
    ) = TjenestepensjonSimuleringPre2025Service(tpClient, spkStillingsprosent, spkTjenestepensjon)

    // --- Happy path ---

    test("simuler returns result from SPK when all conditions are met") {
        val expectedResult = SimulerOffentligTjenestepensjonResult(
            tpnr = "3010", navnOrdning = "SPK", utbetalingsperiodeListe = emptyList()
        )
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } returns expectedResult
        }

        val result = service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())

        result shouldBe expectedResult
    }

    test("simuler passes stillingsprosent and spkMedlemskap to SPK service") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } returns SimulerOffentligTjenestepensjonResult(
                tpnr = "3010", navnOrdning = "SPK"
            )
        }

        service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())

        verify { spkTjenestepensjon.simulerOffentligTjenestepensjon(any(), any(), any()) }
    }

    // --- No TP-forhold ---

    test("simuler returns ikkeMedlem when no TP-forhold found") {
        val tpClient = tpregisteretClient(forhold = emptyList())

        val result = service(tpClient = tpClient).simuler(spec())

        result.brukerErIkkeMedlemAvTPOrdning shouldBe true
        result.tpnr shouldBe ""
    }

    // --- No TSS ID ---

    test("simuler returns ikkeMedlem when tssId is null for all forhold") {
        val tpClient = tpregisteretClient(
            forhold = listOf(spkForhold()),
            tssIdMap = mapOf("3010" to null)
        )

        val result = service(tpClient = tpClient).simuler(spec())

        result.brukerErIkkeMedlemAvTPOrdning shouldBe true
    }

    // --- No supported TP-ordning ---

    test("simuler returns tpOrdningStoettesIkke when no SPK ordning found") {
        val tpClient = tpregisteretClient(
            forhold = listOf(TpForhold(tpNr = "9999", navn = "Annen ordning", datoSistOpptjening = null)),
            tssIdMap = mapOf("9999" to "tss-9999")
        )

        val result = service(tpClient = tpClient).simuler(spec())

        result.brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
    }

    test("simuler finds SPK membership with tpNr 3060") {
        val tpClient = tpregisteretClient(
            forhold = listOf(TpForhold(tpNr = "3060", navn = "SPK 3060", datoSistOpptjening = null)),
            tssIdMap = mapOf("3060" to "tss-3060")
        )

        val result = service(tpClient = tpClient).simuler(spec())

        result.tpnr shouldBe "3010"
    }

    // --- Empty stillingsprosent ---

    test("simuler throws RuntimeException when stillingsprosent is empty") {
        val spkStillingsprosent = mockk<SPKStillingsprosentService> {
            every { getStillingsprosentListe(pid.value, any()) } returns emptyList()
        }

        shouldThrow<RuntimeException> {
            service(spkStillingsprosent = spkStillingsprosent).simuler(spec())
        }
    }

    // --- BrukerKvalifisererIkkeTilTjenestepensjonException ---

    test("simuler rethrows BrukerKvalifisererIkkeTilTjenestepensjonException") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws
                    BrukerKvalifisererIkkeTilTjenestepensjonException("ikke kvalifisert")
        }

        shouldThrow<BrukerKvalifisererIkkeTilTjenestepensjonException> {
            service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())
        }
    }

    // --- Generic exception ---

    test("simuler rethrows generic exception from SPK service") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws RuntimeException("SPK error")
        }

        shouldThrow<RuntimeException> {
            service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())
        }
    }

    // --- filterFnr ---

    test("filterFnr replaces 11-digit numbers") {
        val result = TjenestepensjonSimuleringPre2025Service.filterFnr("pid=12345678910 data")

        result shouldBe "pid=***** data"
    }

    test("filterFnr does not replace shorter numbers") {
        val result = TjenestepensjonSimuleringPre2025Service.filterFnr("pid=1234567890 data")

        result shouldBe "pid=1234567890 data"
    }
})
