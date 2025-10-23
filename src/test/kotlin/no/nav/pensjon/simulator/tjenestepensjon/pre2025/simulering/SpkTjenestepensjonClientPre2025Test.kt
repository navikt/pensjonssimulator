package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.SivilstandCodeEnumDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class SpkTjenestepensjonClientPre2025Test : StringSpec({

    val server = MockWebServer().also { it.start() }
    val baseUrl = server.url("/").toString().removeSuffix("/") // WebClient wants no trailing slash sometimes

    val traceAid = mockk<no.nav.pensjon.simulator.tech.trace.TraceAid>()
    val sporingslogg = mockk<SporingsloggService>()

    // EgressAccess is a Kotlin object → mock its function
    mockkObject(EgressAccess)
    every { EgressAccess.token(any()) } returns mockk(relaxed = true) {
        every { value } returns "token123"
    }
    every { traceAid.callId() } returns "call-123"
    every { sporingslogg.logUtgaaendeRequest(Organisasjoner.SPK,any<Pid>(), any<String>()) } just runs

    val client = SpkTjenestepensjonClientPre2025(
        baseUrl = baseUrl,
        retryAttempts = "0",
        webClientBuilder = WebClient.builder(),
        traceAid = traceAid,
        sporingsloggService = sporingslogg
    )

    val req = HentPrognoseRequestDto(
        fnr = "12345678901",
        fodselsdato = LocalDate.of(1955, 1, 1),
        sisteTpnr = "3010",
        sivilstandkode = SivilstandCodeEnumDto.UGIF,
        inntektListe = emptyList(),
        simuleringsperiodeListe = emptyList(),
        simuleringsdataListe = emptyList()
    )

    val tpDto = TpOrdningFullDto("SPK", "3010", LocalDate.now(), "12345")

    afterSpec {
        server.shutdown()
    }

    "1) getResponse returns body from server" {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(RESPONSE_BODY)
        )

        val res = client.getPrognose(req, tpDto)

        res.tpnr shouldBe "3010"
        res.utbetalingsperiodeListe.size shouldBe 5
        with (res.utbetalingsperiodeListe[0]!!) {
            arligUtbetaling shouldBe 333043.0
            datoFom.toString() shouldBe "2026-08-01"
            datoTom?.toString() shouldBe "2027-07-31"
            uttaksgrad shouldBe 92
            ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AFP
        }
        with (res.utbetalingsperiodeListe[1]!!) {
            arligUtbetaling shouldBe 485760.0
            datoFom.toString() shouldBe "2027-08-01"
            datoTom?.toString() shouldBe "2029-07-31"
            uttaksgrad shouldBe 92
            ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AFP
        }
        with (res.utbetalingsperiodeListe[2]!!) {
            arligUtbetaling shouldBe 181332.0
            datoFom.toString() shouldBe "2029-08-01"
            datoTom?.toString() shouldBe "2029-12-31"
            uttaksgrad shouldBe 100
            ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
        }
        with (res.utbetalingsperiodeListe[3]!!) {
            arligUtbetaling shouldBe 180828.0
            datoFom.toString() shouldBe "2030-01-01"
            datoTom?.toString() shouldBe "2030-12-31"
            uttaksgrad shouldBe 100
            ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
        }
        with (res.utbetalingsperiodeListe[4]!!) {
            arligUtbetaling shouldBe 180504.0
            datoFom.toString() shouldBe "2031-01-01"
            datoTom shouldBe null
            uttaksgrad shouldBe 100
            ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
        }
        res.leverandorUrl shouldBe null
        res.navnOrdning shouldBe "Statens pensjonskasse"
        res.inkluderteOrdningerListe shouldBe listOf("3010")
        res.brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe false
        res.brukerErIkkeMedlemAvTPOrdning shouldBe false


        val call = server.takeRequest()
        call.path shouldBe "/nav/pensjon/prognose/v1"
        call.getHeader("Authorization") shouldBe "Bearer token123"
        call.getHeader(CustomHttpHeaders.CALL_ID) shouldBe "call-123"
    }

    "2) getResponse returns fallback when body is empty (204)" {
        server.enqueue(MockResponse().setResponseCode(204)) // Mono.empty -> block() returns null

        val res = client.getPrognose(req, tpDto)

        // Fallback is constructed as HentPrognoseResponseDto(request.sisteTpnr, tpOrdning.tpNr)
        res.tpnr shouldBe "3010"
    }
}) {
    companion object {
        private const val RESPONSE_BODY = """
        {
          "tpnr": "3010",
          "navnOrdning": "Statens pensjonskasse",
          "inkluderteOrdningerListe": ["3010"],
          "leverandorUrl": null,
          "utbetalingsperiodeListe": [
            {
              "uttaksgrad": 92,
              "arligUtbetaling": 333043.0,
              "datoFom": "2026-08-01",
              "datoTom": "2027-07-31",
              "ytelsekode": "AFP"
            },
            {
              "uttaksgrad": 92,
              "arligUtbetaling": 485760.0,
              "datoFom": "2027-08-01",
              "datoTom": "2029-07-31",
              "ytelsekode": "AFP"
            },
            {
              "uttaksgrad": 100,
              "arligUtbetaling": 181332.0,
              "datoFom": "2029-08-01",
              "datoTom": "2029-12-31",
              "ytelsekode": "AP"
            },
            {
              "uttaksgrad": 100,
              "arligUtbetaling": 180828.0,
              "datoFom": "2030-01-01",
              "datoTom": "2030-12-31",
              "ytelsekode": "AP"
            },
            {
              "uttaksgrad": 100,
              "arligUtbetaling": 180504.0,
              "datoFom": "2031-01-01",
              "datoTom": null,
              "ytelsekode": "AP"
            }
          ],
          "brukerErIkkeMedlemAvTPOrdning": false,
          "brukerErMedlemAvTPOrdningSomIkkeStoettes": false
        }
    """
    }
}

