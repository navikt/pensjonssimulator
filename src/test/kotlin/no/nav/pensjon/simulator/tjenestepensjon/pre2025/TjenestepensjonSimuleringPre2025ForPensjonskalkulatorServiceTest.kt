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
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.forhold.client.TjenestepensjonsforholdClient
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pre2025TjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SivilstandKode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.StillingsprosentService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import java.time.LocalDate
import java.time.Month

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
        Tjenestepensjonsforhold(tpNr = tpNr, navn = navn, datoSistOpptjening = null)

    fun stillingsprosent() = Stillingsprosent(
        datoFom = LocalDate.of(2020, 1, 1),
        datoTom = null,
        stillingsprosent = 100.0,
        aldersgrense = 67,
        faktiskHovedlonn = "500000",
        stillingsuavhengigTilleggslonn = null
    )

    fun tjenestepensjonsforholdClient(
        forhold: List<Tjenestepensjonsforhold> = listOf(spkForhold()),
        tssIdMap: Map<String, String?> = mapOf("3010" to "tss-3010")
    ) = mockk<TjenestepensjonsforholdClient> {
        every { findAlleTpForhold(pid) } returns forhold
        tssIdMap.forEach { (tpNr, tssId) ->
            every { findTssId(tpNr) } returns tssId
        }
    }

    fun featureToggleService() = mockk<FeatureToggleService> {
        every { isEnabled(any()) } returns false
    }

    fun service(
        tpClient: TjenestepensjonsforholdClient = tjenestepensjonsforholdClient(),
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
    ) = TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService(
        tpClient, stillingsprosentService, simuleringService, featureToggleService()
    )

    // --- Happy path ---

    should("return result from SPK when all conditions are met") {
        val expectedResult = SimulerOffentligTjenestepensjonResult(
            tpnr = "3010", navnOrdning = "SPK", utbetalingsperiodeListe = emptyList()
        )
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } returns expectedResult
        }

        val result = service(simuleringService = simuleringService).simuler(spec())

        with(result) {
            tpnr shouldBe "3010"
            navnOrdning shouldBe "SPK"
        }
    }

    // --- No TP-forhold ---

    should("return ikkeMedlem when no TP-forhold found") {
        val tpClient = tjenestepensjonsforholdClient(forhold = emptyList())

        val result = service(tpClient = tpClient).simuler(spec())

        with(result) {
            brukerErIkkeMedlemAvTPOrdning shouldBe true
            tpnr shouldBe ""
        }
    }

    // --- No TSS ID ---

    should("return ikkeMedlem when TSS-ID is undefined for all forhold") {
        val tpClient = tjenestepensjonsforholdClient(
            forhold = listOf(spkForhold()),
            tssIdMap = mapOf("3010" to null)
        )

        service(tpClient = tpClient).simuler(spec()).brukerErIkkeMedlemAvTPOrdning shouldBe true
    }

    // --- No supported TP-ordning ---

    should("return tpOrdningStoettesIkke with ordning names when no TP-ordning found") {
        val tpClient = tjenestepensjonsforholdClient(
            forhold = listOf(Tjenestepensjonsforhold(tpNr = "9999", navn = "Annen ordning", datoSistOpptjening = null)),
            tssIdMap = mapOf("9999" to "tss-9999")
        )

        val result = service(tpClient = tpClient).simuler(spec())

        with(result) {
            brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
            relevanteTpOrdninger shouldBe listOf("Annen ordning")
        }
    }

    should("find SPK membership with TP-nummer 3060") {
        val tpClient = tjenestepensjonsforholdClient(
            forhold = listOf(Tjenestepensjonsforhold(tpNr = "3060", navn = "SPK 3060", datoSistOpptjening = null)),
            tssIdMap = mapOf("3060" to "tss-3060")
        )

        service(tpClient = tpClient).simuler(spec()).tpnr shouldBe "3010"
    }

    // --- Empty stillingsprosent ---

    should("return TEKNISK_FEIL result when stillingsprosent is empty") {
        val simuleringService = mockk<StillingsprosentService> {
            every { getStillingsprosentListe(pid, any()) } returns emptyList()
        }

        val result = service(stillingsprosentService = simuleringService).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.TEKNISK_FEIL
            tpnr shouldBe "3010"
            navnOrdning shouldBe "SPK"
            utbetalingsperiodeListe shouldBe emptyList()
        }
    }

    // --- TjenestepensjonException: BEREGNING_GIR_NULL_UTBETALING ---

    should("return zero-amount perioder when SPK returns BEREGNING_GIR_NULL_UTBETALING") {
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws TjenestepensjonException(
                message = "oops",
                resultat = TjenestepensjonSimuleringsresultatVedFeil(
                    feilkode = Feilkode.BEREGNING_GIR_NULL_UTBETALING,
                    utbetalingsperiodeListe = listOf(
                        Utbetalingsperiode(
                            datoFom = LocalDate.of(2025, Month.JANUARY, 1),
                            datoTom = LocalDate.of(2026, Month.DECEMBER, 31),
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AFP
                        ),
                        Utbetalingsperiode(
                            datoFom = LocalDate.of(2027, Month.JANUARY, 1),
                            datoTom = null,
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AP
                        )
                    )
                )
            )
        }

        val result = service(simuleringService = simuleringService).simuler(spec())

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

    // --- TjenestepensjonException: OPPFYLLER_IKKE_INNGANGSVILKAAR ---

    should("return result with feilkode for OPPFYLLER_IKKE_INNGANGSVILKAAR") {
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws TjenestepensjonException(
                message = "oops",
                resultat = TjenestepensjonSimuleringsresultatVedFeil(
                    feilkode = Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR
                )
            )
        }

        val result = service(simuleringService = simuleringService).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR
            utbetalingsperiodeListe shouldBe emptyList()
            tpnr shouldBe "3010"
        }
    }

    // --- TjenestepensjonException: TEKNISK_FEIL ---

    should("return result with TEKNISK_FEIL for known technical error codes") {
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws TjenestepensjonException(
                message = "oops",
                resultat = TjenestepensjonSimuleringsresultatVedFeil(feilkode = Feilkode.TEKNISK_FEIL)
            )
        }

        val result = service(simuleringService = simuleringService).simuler(spec())

        with(result) {
            feilkode shouldBe Feilkode.TEKNISK_FEIL
            utbetalingsperiodeListe shouldBe emptyList()
        }
    }

    // --- EgressException before SPK membership found ---

    should("rethrow EgressException when SPK-medlemskap is null") {
        val tpClient = mockk<TjenestepensjonsforholdClient> {
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
        val simuleringService = mockk<Pre2025TjenestepensjonService> {
            every { simulerOffentligTjenestepensjon(any(), any(), any()) } throws EgressException(
                """{"message":"error without code"}"""
            )
        }

        shouldThrow<EgressException> {
            service(simuleringService = simuleringService).simuler(spec())
        }
    }
})