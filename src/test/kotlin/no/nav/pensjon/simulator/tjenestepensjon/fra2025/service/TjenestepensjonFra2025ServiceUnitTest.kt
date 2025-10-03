package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
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
    val stos = SisteTpOrdningNavService()

    val service = TjenestepensjonFra2025Service(tp, spk, klp, stos)

    beforeTest {
        clearMocks(tp, spk, klp)
    }

    test("simuler success fra spk") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns listOf(dummyTpOrdning(spkTpNummer))
        every { spk.simuler(req, spkTpNummer) } returns dummyResult("spk", spkTpNummer)

        val res = service.simuler(req)

        res.second.isSuccess.shouldBeTrue()
        val tpRes = res.second.getOrNull().shouldNotBeNull()
        tpRes.tpLeverandoer shouldBe "spk"
        tpRes.tpNummer shouldBe spkTpNummer
        tpRes.ordningsListe.size shouldBe 1
        tpRes.utbetalingsperioder.size shouldBe 1
    }

    test("simuler failure fra spk") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns listOf(dummyTpOrdning(spkTpNummer))
        every { spk.simuler(req, spkTpNummer) } returns
                Result.failure(WebClientResponseException("Failed to simulate", 500, "error", null, null, null))

        val res = service.simuler(req)

        res.second.isFailure.shouldBeTrue()
        val ex = res.second.exceptionOrNull().shouldNotBeNull()
        ex.message shouldBe "Failed to simulate"
    }

    test("simuler naar tp-ordning ikke stoettes") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns listOf(dummyTpOrdning("9999"))

        val res = service.simuler(req)

        res.second.isFailure.shouldBeTrue()
        (res.second.exceptionOrNull() is TpOrdningStoettesIkkeException).shouldBeTrue()
    }

    test("simuler tp naar bruker ikke er medlem i tp ordning") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns emptyList()

        val res = service.simuler(req)

        res.second.isFailure.shouldBeTrue()
        (res.second.exceptionOrNull() is BrukerErIkkeMedlemException).shouldBeTrue()
    }

    test("simuler naar tpregisteret feilet") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } throws ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        try {
            service.simuler(req)
            throw AssertionError("Expected ResponseStatusException")
        } catch (e: ResponseStatusException) {
            e.statusCode.is5xxServerError.shouldBeTrue()
        }
    }

    test("simuler success fra klp 4082") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns listOf(dummyTpOrdning(klpAskerKommunalePensjonskasseTpNummer))
        every { klp.simuler(req, klpAskerKommunalePensjonskasseTpNummer) } returns dummyResult("klp", klpAskerKommunalePensjonskasseTpNummer)

        val res = service.simuler(req)

        res.second.isSuccess.shouldBeTrue()
        val tpRes = res.second.getOrNull().shouldNotBeNull()
        tpRes.tpLeverandoer shouldBe "klp"
        tpRes.tpNummer shouldBe klpAskerKommunalePensjonskasseTpNummer
        tpRes.ordningsListe.size shouldBe 1
        tpRes.utbetalingsperioder.size shouldBe 1
    }

    test("simuler success fra klp 3200") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns listOf(dummyTpOrdning(klpTpNummer))
        every { klp.simuler(req, klpTpNummer) } returns dummyResult("klp", klpTpNummer)

        val res = service.simuler(req)

        res.second.isSuccess.shouldBeTrue()
        val tpRes = res.second.getOrNull().shouldNotBeNull()
        tpRes.tpLeverandoer shouldBe "klp"
        tpRes.tpNummer shouldBe klpTpNummer
        tpRes.ordningsListe.size shouldBe 1
        tpRes.utbetalingsperioder.size shouldBe 1
    }

    test("simulering feiler naar spk og klp returnerer tomt resultat") {
        val req = dummyRequest("1963-02-05")
        every { tp.findAlleTpForhold(req.pid) } returns listOf(dummyTpOrdning(spkTpNummer), dummyTpOrdning(klpAskerKommunalePensjonskasseTpNummer))
        every { spk.simuler(req, spkTpNummer) } returns Result.failure(TomSimuleringFraTpOrdningException(spkTpNummer))
        every { klp.simuler(req, klpAskerKommunalePensjonskasseTpNummer) } returns Result.failure(TomSimuleringFraTpOrdningException(klpAskerKommunalePensjonskasseTpNummer))

        val res = service.simuler(req)

        res.second.isFailure.shouldBeTrue()
        (res.second.exceptionOrNull() is TomSimuleringFraTpOrdningException).shouldBeTrue()
    }

    test("simuler naar bruker er apoteker") {
        val req = dummyRequest("1963-02-05", erApoteker = true)
        val tpOrdninger = listOf(dummyTpOrdning(spkTpNummer))
        every { tp.findAlleTpForhold(req.pid) } returns tpOrdninger

        val res = service.simuler(req)

        res.second.isFailure.shouldBeTrue()
        val ex = res.second.exceptionOrNull().shouldNotBeNull()
        (ex is TpOrdningStoettesIkkeException).shouldBeTrue()
        ex.message shouldBe "Apoteker støtter ikke simulering av tjenestepensjon v2025"
        (ex as TpOrdningStoettesIkkeException).tpOrdning shouldBe "Apoteker"
        res.first shouldBe tpOrdninger.map { it.navn }
    }
}) {

    companion object {
        const val spkTpNummer = "3010"
        const val klpTpNummer = "3200"
        const val klpAskerKommunalePensjonskasseTpNummer = "4082"

        fun dummyRequest(
            foedselsdato: String,
            brukerBaOmAfp: Boolean = false,
            erApoteker: Boolean = false
        ) = SimulerOffentligTjenestepensjonFra2025SpecV1(
            pid = "12345678910",
            foedselsdato = LocalDate.parse(foedselsdato),
            uttaksdato = LocalDate.parse("2025-03-01"),
            sisteInntekt = 500000,
            aarIUtlandetEtter16 = 0,
            brukerBaOmAfp = brukerBaOmAfp,
            epsPensjon = false,
            eps2G = false,
            fremtidigeInntekter = emptyList(),
            erApoteker = erApoteker
        )

        fun dummyTpOrdning(tpNummer: String) =
            TpForhold(tpNummer, "Statens pensjonskasse", null)

        fun dummyResult(leverandoer: String, tpNummer: String) =
            Result.success(
                SimulertTjenestepensjonMedMaanedsUtbetalinger(
                    tpLeverandoer = leverandoer,
                    tpNummer = tpNummer,
                    ordningsListe = listOf(Ordning(spkTpNummer)),
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
