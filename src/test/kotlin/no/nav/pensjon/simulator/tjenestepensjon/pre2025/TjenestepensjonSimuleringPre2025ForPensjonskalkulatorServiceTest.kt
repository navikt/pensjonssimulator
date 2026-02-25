package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SivilstandKode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SpkStillingsprosentService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import java.time.LocalDate

class TjenestepensjonSimuleringPre2025ForPensjonskalkulatorServiceTest : ShouldSpec({

    val pid = Pid("12345678910")
    val foedselsdato = LocalDate.of(1963, 1, 15)

    fun spec() = TjenestepensjonSimuleringPre2025Spec(
        pid = pid,
        foedselsdato = foedselsdato,
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
        stillingsuavhengigTilleggslonn = null
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

    fun featureToggleService() = mockk<FeatureToggleService> {
        every { isEnabled(any()) } returns false
    }

    fun service(
        tpClient: TpregisteretClient = tpregisteretClient(),
        spkStillingsprosent: SpkStillingsprosentService = mockk {
            every { getStillingsprosentListe(pid, any()) } returns listOf(stillingsprosent())
        },
        spkTjenestepensjon: SPKTjenestepensjonServicePre2025 = mockk {
            every {
                simulerOffentligTjenestepensjon(any(), any(), any())
            } returns SimulerOffentligTjenestepensjonResult(
                tpnr = "3010",
                navnOrdning = "SPK",
                utbetalingsperiodeListe = emptyList()
            )
        }
    ) = TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService(
        tpClient, spkStillingsprosent, spkTjenestepensjon, featureToggleService()
    )

    // --- Happy path ---

    should("return result from SPK when all conditions are met") {
        val expectedResult = SimulerOffentligTjenestepensjonResult(
            tpnr = "3010", navnOrdning = "SPK", utbetalingsperiodeListe = emptyList()
        )
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } returns expectedResult
        }

        val result = service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())

        with(result) {
            tpnr shouldBe "3010"
            navnOrdning shouldBe "SPK"
        }
    }

    // --- No TP-forhold ---

    should("return ikkeMedlem when no TP-forhold found") {
        val tpClient = tpregisteretClient(forhold = emptyList())

        val result = service(tpClient = tpClient).simuler(spec())

        with(result) {
            brukerErIkkeMedlemAvTPOrdning shouldBe true
            tpnr shouldBe ""
        }
    }

    // --- No TSS ID ---

    should("return ikkeMedlem when TSS-ID is undefined for all forhold") {
        val tpClient = tpregisteretClient(
            forhold = listOf(spkForhold()),
            tssIdMap = mapOf("3010" to null)
        )

        service(tpClient = tpClient).simuler(spec()).brukerErIkkeMedlemAvTPOrdning shouldBe true
    }

    // --- No supported TP-ordning ---

    should("return tpOrdningStoettesIkke with ordning names when no TP-ordning found") {
        val tpClient = tpregisteretClient(
            forhold = listOf(TpForhold(tpNr = "9999", navn = "Annen ordning", datoSistOpptjening = null)),
            tssIdMap = mapOf("9999" to "tss-9999")
        )

        val result = service(tpClient = tpClient).simuler(spec())

        with(result) {
            brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
            relevanteTpOrdninger shouldBe listOf("Annen ordning")
        }
    }

    should("find SPK membership with TP-nummer 3060") {
        val tpClient = tpregisteretClient(
            forhold = listOf(TpForhold(tpNr = "3060", navn = "SPK 3060", datoSistOpptjening = null)),
            tssIdMap = mapOf("3060" to "tss-3060")
        )

        service(tpClient = tpClient).simuler(spec()).tpnr shouldBe "3010"
    }

    // --- Empty stillingsprosent ---

    should("return TEKNISK_FEIL result when stillingsprosent is empty") {
        val spkStillingsprosent = mockk<SpkStillingsprosentService> {
            every { getStillingsprosentListe(pid, any()) } returns emptyList()
        }

        val result = service(spkStillingsprosent = spkStillingsprosent).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.TEKNISK_FEIL
            tpnr shouldBe "3010"
            navnOrdning shouldBe "SPK"
            utbetalingsperiodeListe shouldBe emptyList()
        }
    }

    // --- EgressException: BEREGNING_GIR_NULL_UTBETALING ---

    should("return zero-amount perioder when SPK returns BEREGNING_GIR_NULL_UTBETALING") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws EgressException(
                """{"errorCode":"CALC002","message":"Validation problem: Beregning gir 0 i utbetaling."}"""
            )
        }

        val result = service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.BEREGNING_GIR_NULL_UTBETALING
            utbetalingsperiodeListe shouldHaveSize 2
            with(utbetalingsperiodeListe[0]) {
                arligUtbetaling shouldBe 0.0
                ytelsekode shouldBe YtelseCode.AFP
            }
            with(utbetalingsperiodeListe[1]) {
                arligUtbetaling shouldBe 0.0
                ytelsekode shouldBe YtelseCode.AP
                datoTom shouldBe null
            }
        }
    }

    // --- EgressException: OPPFYLLER_IKKE_INNGANGSVILKAAR ---

    should("return result with feilkode for OPPFYLLER_IKKE_INNGANGSVILKAAR") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws EgressException(
                """{"errorCode":"CALC002","message":"Validation problem: Tjenestetid mindre enn 3 år."}"""
            )
        }

        val result = service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR
            utbetalingsperiodeListe shouldBe emptyList()
            tpnr shouldBe "3010"
        }
    }

    // --- EgressException: TEKNISK_FEIL ---

    should("return result with TEKNISK_FEIL for known technical error codes") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws EgressException(
                """{"errorCode":"CALC001","message":"Some technical error"}"""
            )
        }

        val result = service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.TEKNISK_FEIL
            utbetalingsperiodeListe shouldBe emptyList()
        }
    }

    // --- EgressException: unknown error code ---

    should("return TEKNISK_FEIL for unknown error code") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws EgressException(
                """{"errorCode":"UNKNOWN","message":"Unknown error"}"""
            )
        }

        service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec()).feilkode shouldBe Feilkode.TEKNISK_FEIL
    }

    // --- EgressException before SPK membership found ---

    should("rethrow EgressException when SPK-medlemskap is null") {
        val tpClient = mockk<TpregisteretClient> {
            every { findAlleTpForhold(pid) } throws EgressException(
                """{"errorCode":"CALC001","message":"error"}"""
            )
        }

        shouldThrow<EgressException> {
            service(tpClient = tpClient).simuler(spec())
        }
    }

    // --- EgressException with missing errorCode ---

    should("rethrow EgressException when error code is undefined") {
        val spkTjenestepensjon = mockk<SPKTjenestepensjonServicePre2025> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws EgressException(
                """{"message":"error without code"}"""
            )
        }

        shouldThrow<EgressException> {
            service(spkTjenestepensjon = spkTjenestepensjon).simuler(spec())
        }
    }
})
