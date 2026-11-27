package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import no.nav.pensjon.simulator.tjenestepensjon.forhold.client.TjenestepensjonsforholdClient
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.BrukerKvalifisererIkkeTilTjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pre2025TjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SivilstandKode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.StillingsprosentService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
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

    fun tpForhold(tpNr: String = "3010", navn: String = "SPK") =
        Tjenestepensjonsforhold(tpNr = tpNr, navn = navn, datoSistOpptjening = null)

    fun stillingsprosent() = Stillingsprosent(
        datoFom = LocalDate.of(2020, 1, 1),
        datoTom = null,
        stillingsprosent = 100.0,
        aldersgrense = 67,
        faktiskHovedlonn = "500000",
        stillingsuavhengigTilleggslonn = null
    )

    fun tpForholdClient(
        tpForholdListe: List<Tjenestepensjonsforhold> = listOf(tpForhold()),
        tssIdMap: Map<String, String?> = mapOf("3010" to "tss-3010")
    ) = mockk<TjenestepensjonsforholdClient> {
        every { findAlleTpForhold(pid) } returns tpForholdListe
        tssIdMap.forEach { (tpNr, tssId) -> every { findTssId(tpNr) } returns tssId }
    }

    fun service(
        tpForholdClient: TjenestepensjonsforholdClient = tpForholdClient(),
        stillingsprosentService: StillingsprosentService = mockk {
            every { getStillingsprosentListe(pid, any()) } returns listOf(stillingsprosent())
        },
        simuleringService: Pre2025TjenestepensjonService = mockk {
            every {
                simulerOffentligTjenestepensjon(any(), any(), any())
            } returns SimulerOffentligTjenestepensjonResult(
                tpnr = "3010",
                navnOrdning = "SPK",
                utbetalingsperiodeListe = emptyList()
            )
        }
    ) = TjenestepensjonSimuleringPre2025Service(tpForholdClient, stillingsprosentService, simuleringService)

    // --- Happy path ---

    test("simuler returns result from SPK when all conditions are met") {
        val expectedResult = SimulerOffentligTjenestepensjonResult(
            tpnr = "3010", navnOrdning = "SPK", utbetalingsperiodeListe = emptyList()
        )
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } returns expectedResult
        }

        service(simuleringService = simuleringService).simuler(spec()) shouldBe expectedResult
    }

    test("simuler passes stillingsprosent and spkMedlemskap to SPK service") {
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every {
                simulerOffentligTjenestepensjon(any(), any(), any())
            } returns SimulerOffentligTjenestepensjonResult(
                tpnr = "3010", navnOrdning = "SPK"
            )
        }

        service(simuleringService = simuleringService).simuler(spec())

        verify { simuleringService.simulerOffentligTjenestepensjon(any(), any(), any()) }
    }

    // --- No TP-forhold ---

    test("simuler returns ikkeMedlem when no TP-forhold found") {
        val tpForholdClient = tpForholdClient(tpForholdListe = emptyList())

        val result = service(tpForholdClient = tpForholdClient).simuler(spec())

        with(result) {
            brukerErIkkeMedlemAvTPOrdning shouldBe true
            tpnr shouldBe ""
        }
    }

    // --- No TSS ID ---

    test("simuler returns ikkeMedlem when tssId is null for all forhold") {
        val tpForholdClient = tpForholdClient(
            tpForholdListe = listOf(tpForhold()),
            tssIdMap = mapOf("3010" to null)
        )

        service(tpForholdClient = tpForholdClient).simuler(spec()).brukerErIkkeMedlemAvTPOrdning shouldBe true
    }

    // --- No supported TP-ordning ---

    test("simuler returns tpOrdningStoettesIkke when no SPK ordning found") {
        val tpForholdClient = tpForholdClient(
            tpForholdListe = listOf(
                Tjenestepensjonsforhold(
                    tpNr = "9999",
                    navn = "Annen ordning",
                    datoSistOpptjening = null
                )
            ),
            tssIdMap = mapOf("9999" to "tss-9999")
        )

        service(tpForholdClient = tpForholdClient).simuler(spec())
            .brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
    }

    test("simuler finds SPK membership with tpNr 3060") {
        val tpForholdClient = tpForholdClient(
            tpForholdListe = listOf(
                Tjenestepensjonsforhold(
                    tpNr = "3060",
                    navn = "SPK 3060",
                    datoSistOpptjening = null
                )
            ),
            tssIdMap = mapOf("3060" to "tss-3060")
        )

        service(tpForholdClient = tpForholdClient).simuler(spec()).tpnr shouldBe "3010"
    }

    // --- Empty stillingsprosent ---

    test("simuler throws RuntimeException when stillingsprosent is empty") {
        val stillingsprosentService = mockk<StillingsprosentService> {
            every { getStillingsprosentListe(pid, any()) } returns emptyList()
        }

        shouldThrow<RuntimeException> {
            service(stillingsprosentService = stillingsprosentService).simuler(spec())
        }
    }

    // --- BrukerKvalifisererIkkeTilTjenestepensjonException ---

    test("simuler rethrows BrukerKvalifisererIkkeTilTjenestepensjonException") {
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every {
                simulerOffentligTjenestepensjon(any(), any(), any())
            } throws BrukerKvalifisererIkkeTilTjenestepensjonException("ikke kvalifisert")
        }

        shouldThrow<BrukerKvalifisererIkkeTilTjenestepensjonException> {
            service(simuleringService = simuleringService).simuler(spec())
        }
    }

    // --- Generic exception ---

    test("simuler rethrows generic exception from SPK service") {
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws RuntimeException("SPK error")
        }

        shouldThrow<RuntimeException> {
            service(simuleringService = simuleringService).simuler(spec())
        }
    }

    // --- filterFnr ---

    test("filterFnr replaces 11-digit numbers") {
        TjenestepensjonSimuleringPre2025Service.filterFnr("pid=12345678910 data") shouldBe "pid=***** data"
    }

    test("filterFnr does not replace shorter numbers") {
        TjenestepensjonSimuleringPre2025Service.filterFnr("pid=1234567890 data") shouldBe "pid=1234567890 data"
    }
})