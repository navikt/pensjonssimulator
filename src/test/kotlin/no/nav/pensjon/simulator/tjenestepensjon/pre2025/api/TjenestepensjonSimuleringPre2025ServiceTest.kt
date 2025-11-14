package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringPre2025Service
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1.YtelseCode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.BrukerKvalifisererIkkeTilTjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SPKStillingsprosentService
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import java.time.LocalDate

class TjenestepensjonSimuleringPre2025ServiceTest : StringSpec({

    val fnr = "12345678901"
    val pid = Pid(fnr)
    val tpClient = mockk<TpregisteretClient>()
    val stillingsprosentService = mockk<SPKStillingsprosentService>()
    val spkService = mockk<SPKTjenestepensjonServicePre2025>()

    val service = TjenestepensjonSimuleringPre2025Service(
        tpregisteretClient = tpClient,
        spkStillingsprosentService = stillingsprosentService,
        spkTjenestepensjonServicePre2025 = spkService
    )

    val spec = TjenestepensjonSimuleringPre2025Spec(
        pid,
        foedselsdato = LocalDate.of(1990, 1, 1),
        sisteTpOrdningsTpNummer = "3010",
        simulertOffentligAfp = null,
        simulertPrivatAfp = null,
        sivilstand = SivilstandKode.ENKE,
        inntekter = emptyList(),
        pensjonsbeholdningsperioder = emptyList(),
        simuleringsperioder = emptyList(),
        simuleringsdata = emptyList(),
        tpForhold = emptyList()
    )

    "returns ikkeMedlem when no TP-forhold found" {
        every { tpClient.findAlleTpForhold(pid) } returns emptyList()

        service.simuler(spec) shouldBe SimulerOffentligTjenestepensjonResultV1.ikkeMedlem()
    }

    "returns tpOrdningStoettesIkke when no SPK-ordning present" {
        val forhold = TpForhold("9999", "MPK", LocalDate.now())
        every { tpClient.findAlleTpForhold(pid) } returns listOf(forhold)
        every { tpClient.findTssId("9999") } returns "123456"

        service.simuler(spec) shouldBe SimulerOffentligTjenestepensjonResultV1.tpOrdningStoettesIkke()
    }

    "throws RuntimeException when stillingsprosent list is empty" {
        val spk = TpForhold("SPK", "3010", LocalDate.now())
        every { tpClient.findAlleTpForhold(pid) } returns listOf(spk)
        every { tpClient.findTssId("3010") } returns "123321"
        every { stillingsprosentService.getStillingsprosentListe(fnr, any()) } returns emptyList()

        shouldThrow<RuntimeException> { service.simuler(spec) }
    }

    "delegates to SPK service and returns mapped result on success" {
        val spkFullDto = TpOrdningFullDto("SPK", "3010", LocalDate.now(), "123321")
        val spk = TpForhold(spkFullDto.tpNr, spkFullDto.navn, spkFullDto.datoSistOpptjening)
        every { tpClient.findAlleTpForhold(pid) } returns listOf(spk)
        every { tpClient.findTssId(spkFullDto.tpNr) } returns spkFullDto.tssId
        every { stillingsprosentService.getStillingsprosentListe(fnr, spkFullDto) } returns listOf(mockk())
        every {
            spkService.simulerOffentligTjenestepensjon(any(), any(), spkFullDto)
        } returns SimulerOffentligTjenestepensjonResultV1(
            tpnr = spkFullDto.tpNr,
            navnOrdning = spkFullDto.navn,
            inkluderteOrdningerListe = listOf(spkFullDto.navn),
            leverandorUrl = "spk.no",
            utbetalingsperiodeListe = listOf(
                SimulerOffentligTjenestepensjonResultV1.UtbetalingsperiodeV1(
                    uttaksgrad = 100,
                    arligUtbetaling = 120000.0,
                    datoFom = LocalDate.of(2055, 1, 1),
                    datoTom = null,
                    ytelsekode = YtelseCode.AP
                )
            ),
            brukerErIkkeMedlemAvTPOrdning = false,
            brukerErMedlemAvTPOrdningSomIkkeStoettes = false,
        )

        val result = service.simuler(spec)

        with(result) {
            tpnr shouldBe spkFullDto.tpNr
            navnOrdning shouldBe spkFullDto.navn
            inkluderteOrdningerListe shouldBe listOf(spkFullDto.navn)
            leverandorUrl shouldBe "spk.no"
            utbetalingsperiodeListe shouldHaveSize 1
            with(utbetalingsperiodeListe[0]!!) {
                uttaksgrad shouldBe 100
                arligUtbetaling shouldBe 120000.0
                datoFom shouldBe LocalDate.of(2055, 1, 1)
                datoTom shouldBe null
                ytelsekode shouldBe YtelseCode.AP
            }
            brukerErIkkeMedlemAvTPOrdning shouldBe false
            brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe false
        }
    }

    "rethrows BrukerKvalifisererIkkeTilTjenestepensjonException" {
        val tssId = "123321"
        val spkFullDto = TpOrdningFullDto("SPK", "3010", LocalDate.now(), tssId)
        val spk = TpForhold(spkFullDto.tpNr, spkFullDto.navn, spkFullDto.datoSistOpptjening)
        every { tpClient.findAlleTpForhold(pid) } returns listOf(spk)
        every { tpClient.findTssId("3010") } returns "123321"
        every { stillingsprosentService.getStillingsprosentListe(fnr, spkFullDto) } returns listOf(mockk())
        every {
            spkService.simulerOffentligTjenestepensjon(any(), any(), spkFullDto)
        } throws BrukerKvalifisererIkkeTilTjenestepensjonException("ikke kval")

        shouldThrow<BrukerKvalifisererIkkeTilTjenestepensjonException> { service.simuler(spec) }
    }

    "rethrows other exceptions" {
        val tssId = "123321"
        val spkFullDto = TpOrdningFullDto("SPK", "3010", LocalDate.now(), tssId)
        val spk = TpForhold(spkFullDto.tpNr, spkFullDto.navn, spkFullDto.datoSistOpptjening)
        every { tpClient.findAlleTpForhold(pid) } returns listOf(spk)
        every { tpClient.findTssId("3010") } returns "123321"
        every { stillingsprosentService.getStillingsprosentListe(fnr, spkFullDto) } returns listOf(mockk())
        every {
            spkService.simulerOffentligTjenestepensjon(any(), any(), spkFullDto)
        } throws IllegalStateException("boom")

        shouldThrow<IllegalStateException> { service.simuler(spec) }
    }
})
