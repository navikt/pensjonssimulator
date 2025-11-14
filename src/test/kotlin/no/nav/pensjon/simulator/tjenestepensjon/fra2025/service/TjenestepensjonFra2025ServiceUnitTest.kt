package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Maanedsutbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.BrukerErIkkeMedlemException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpOrdningStoettesIkkeException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpTjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning.SisteTpOrdningNavService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SpkTjenestepensjonService
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

class TjenestepensjonFra2025ServiceUnitTest : FunSpec({

    val tp = mockk<TpregisteretClient>(relaxed = true)
    val spk = mockk<SpkTjenestepensjonService>(relaxed = true)
    val klp = mockk<KlpTjenestepensjonService>(relaxed = true)

    val service = TjenestepensjonFra2025Service(
        tpregisteretClient = tp,
        spk = spk,
        klp = klp,
        sisteTpOrdningService = SisteTpOrdningNavService()
    )

    beforeTest {
        clearMocks(tp, spk, klp)
    }

    test("simuler success fra SPK") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns listOf(dummyTpOrdning(SPK_TP_NUMMER))
        every { spk.simuler(spec, SPK_TP_NUMMER) } returns dummyResult("spk", SPK_TP_NUMMER)

        val res = service.simuler(spec)

        res.second.isSuccess.shouldBeTrue()
        val tpRes = res.second.getOrNull().shouldNotBeNull()
        tpRes.tpLeverandoer shouldBe "spk"
        tpRes.tpNummer shouldBe SPK_TP_NUMMER
        tpRes.ordningsListe.size shouldBe 1
        tpRes.utbetalingsperioder.size shouldBe 1
    }

    test("simuler failure fra SPK") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns listOf(dummyTpOrdning(SPK_TP_NUMMER))
        every { spk.simuler(spec, SPK_TP_NUMMER) } returns
                Result.failure(WebClientResponseException("Failed to simulate", 500, "error", null, null, null))

        val res = service.simuler(spec)

        res.second.isFailure.shouldBeTrue()
        val ex = res.second.exceptionOrNull().shouldNotBeNull()
        ex.message shouldBe "Failed to simulate"
    }

    test("simuler når TP-ordning ikke støttes") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns listOf(dummyTpOrdning("9999"))

        val res = service.simuler(spec)

        res.second.isFailure.shouldBeTrue()
        (res.second.exceptionOrNull() is TpOrdningStoettesIkkeException).shouldBeTrue()
    }

    test("simuler tjenestepensjon når bruker ikke er medlem i TP-ordning") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns emptyList()

        val res = service.simuler(spec)

        res.second.isFailure.shouldBeTrue()
        (res.second.exceptionOrNull() is BrukerErIkkeMedlemException).shouldBeTrue()
    }

    test("simuler når TP-registeret feilet") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } throws ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        try {
            service.simuler(spec)
            throw AssertionError("Expected ResponseStatusException")
        } catch (e: ResponseStatusException) {
            e.statusCode.is5xxServerError.shouldBeTrue()
        }
    }

    test("simuler success fra KLP 4082") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns listOf(dummyTpOrdning(KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER))
        every { klp.simuler(spec, KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER) } returns dummyResult("klp", KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER)

        val res = service.simuler(spec)

        res.second.isSuccess.shouldBeTrue()
        val tpRes = res.second.getOrNull().shouldNotBeNull()
        tpRes.tpLeverandoer shouldBe "klp"
        tpRes.tpNummer shouldBe KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER
        tpRes.ordningsListe.size shouldBe 1
        tpRes.utbetalingsperioder.size shouldBe 1
    }

    test("simuler success fra KLP 3200") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns listOf(dummyTpOrdning(KLP_TP_NUMMER))
        every { klp.simuler(spec, KLP_TP_NUMMER) } returns dummyResult("klp", KLP_TP_NUMMER)

        val res = service.simuler(spec)

        res.second.isSuccess.shouldBeTrue()
        val tpRes = res.second.getOrNull().shouldNotBeNull()
        tpRes.tpLeverandoer shouldBe "klp"
        tpRes.tpNummer shouldBe KLP_TP_NUMMER
        tpRes.ordningsListe.size shouldBe 1
        tpRes.utbetalingsperioder.size shouldBe 1
    }

    test("simulering feiler når SPK og KLP returnerer tomt resultat") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { tp.findAlleTpForhold(spec.pid) } returns listOf(dummyTpOrdning(SPK_TP_NUMMER), dummyTpOrdning(KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER))
        every { spk.simuler(spec, SPK_TP_NUMMER) } returns Result.failure(TomSimuleringFraTpOrdningException(SPK_TP_NUMMER))
        every { klp.simuler(spec, KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER) } returns Result.failure(TomSimuleringFraTpOrdningException(KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER))

        val res = service.simuler(spec)

        res.second.isFailure.shouldBeTrue()
        (res.second.exceptionOrNull() is TomSimuleringFraTpOrdningException).shouldBeTrue()
    }

    test("simuler når bruker er apoteker") {
        val spec = dummySpec(foedselsdato = "1963-02-05", gjelderApoteker = true)
        val tpOrdninger = listOf(dummyTpOrdning(SPK_TP_NUMMER))
        every { tp.findAlleTpForhold(spec.pid) } returns tpOrdninger

        val res = service.simuler(spec)

        res.second.isFailure.shouldBeTrue()
        val ex = res.second.exceptionOrNull().shouldNotBeNull()
        (ex is TpOrdningStoettesIkkeException).shouldBeTrue()
        ex.message shouldBe "Apoteker støtter ikke simulering av tjenestepensjon v2025"
        (ex as TpOrdningStoettesIkkeException).tpOrdning shouldBe "Apoteker"
        res.first shouldBe tpOrdninger.map { it.navn }
    }
}) {
    companion object {
        const val SPK_TP_NUMMER = "3010"
        const val KLP_TP_NUMMER = "3200"
        const val KLP_ASKER_KOMMUNALE_PENSJONSKASSE_TP_NUMMER = "4082"

        fun dummySpec(
            foedselsdato: String,
            afpErForespurt: Boolean = false,
            gjelderApoteker: Boolean = false
        ) = OffentligTjenestepensjonFra2025SimuleringSpec(
            pid = Pid("12345678910"),
            foedselsdato = LocalDate.parse(foedselsdato),
            uttaksdato = LocalDate.parse("2025-03-01"),
            sisteInntekt = 500000,
            utlandAntallAar = 0,
            afpErForespurt,
            epsHarPensjon = false,
            epsHarInntektOver2G = false,
            fremtidigeInntekter = emptyList(),
            gjelderApoteker
        )

        fun dummyTpOrdning(tpNummer: String) =
            TpForhold(tpNr = tpNummer, navn = "Statens pensjonskasse", datoSistOpptjening = null)

        fun dummyResult(leverandoer: String, tpNummer: String) =
            Result.success(
                value = SimulertTjenestepensjonMedMaanedsUtbetalinger(
                    tpLeverandoer = leverandoer,
                    tpNummer = tpNummer,
                    ordningsListe = listOf(Ordning(SPK_TP_NUMMER)),
                    utbetalingsperioder = listOf(
                        Maanedsutbetaling(
                            fraOgMedDato = LocalDate.parse("2025-03-01"),
                            fraOgMedAlder = Alder(61, 2),
                            maanedsBeloep = 5000
                        )
                    )
                )
            )
    }
}
