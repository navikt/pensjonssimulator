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
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.SivilstandCodeEnumDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class SpkTjenestepensjonClientPre2025Test : StringSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null
    val traceAid = mockk<TraceAid> { every { callId() } returns "call-123" }

    val sporingslogg = mockk<SporingsloggService> {
        every { logUtgaaendeRequest(Organisasjoner.SPK, any<Pid>(), any<String>()) } just runs
    }

    // EgressAccess is a Kotlin object → mock its function
    mockkObject(EgressAccess)
    every {
        EgressAccess.token(any())
    } returns mockk(relaxed = true) {
        every { value } returns "token123"
    }

    fun client(context: BeanFactory) =
        SpkTjenestepensjonClientPre2025(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            traceAid,
            sporingsloggService = sporingslogg
        )

    beforeSpec {
        WebClient.builder().build().mutate()
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    val request = HentPrognoseRequestDto(
        fnr = "12345678901",
        fodselsdato = LocalDate.of(1955, 1, 1),
        sisteTpnr = "3010",
        sivilstandkode = SivilstandCodeEnumDto.UGIF,
        inntektListe = emptyList(),
        simuleringsperiodeListe = emptyList(),
        simuleringsdataListe = emptyList()
    )

    val tpDto = TpOrdningFullDto("SPK", "3010", LocalDate.now(), "12345")

    "1) getResponse returns body from server" {
        Arrange.webClientContextRunner().run {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(RESPONSE_BODY)
            )

            val result = client(context = it).getPrognose(request, tpDto)

            with(result) {
                tpnr shouldBe "3010"
                utbetalingsperiodeListe.size shouldBe 5
                with(utbetalingsperiodeListe[0]!!) {
                    arligUtbetaling shouldBe 333043.0
                    datoFom.toString() shouldBe "2026-08-01"
                    datoTom?.toString() shouldBe "2027-07-31"
                    uttaksgrad shouldBe 92
                    ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AFP
                }
                with(utbetalingsperiodeListe[1]!!) {
                    arligUtbetaling shouldBe 485760.0
                    datoFom.toString() shouldBe "2027-08-01"
                    datoTom?.toString() shouldBe "2029-07-31"
                    uttaksgrad shouldBe 92
                    ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AFP
                }
                with(utbetalingsperiodeListe[2]!!) {
                    arligUtbetaling shouldBe 181332.0
                    datoFom.toString() shouldBe "2029-08-01"
                    datoTom?.toString() shouldBe "2029-12-31"
                    uttaksgrad shouldBe 100
                    ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
                }
                with(utbetalingsperiodeListe[3]!!) {
                    arligUtbetaling shouldBe 180828.0
                    datoFom.toString() shouldBe "2030-01-01"
                    datoTom?.toString() shouldBe "2030-12-31"
                    uttaksgrad shouldBe 100
                    ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
                }
                with(utbetalingsperiodeListe[4]!!) {
                    arligUtbetaling shouldBe 180504.0
                    datoFom.toString() shouldBe "2031-01-01"
                    datoTom shouldBe null
                    uttaksgrad shouldBe 100
                    ytelsekode shouldBe SimulerOffentligTjenestepensjonResultV1.YtelseCode.AP
                }
                leverandorUrl shouldBe null
                navnOrdning shouldBe "Statens pensjonskasse"
                inkluderteOrdningerListe shouldBe listOf("3010")
                brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe false
                brukerErIkkeMedlemAvTPOrdning shouldBe false
            }
            with(server!!.takeRequest()) {
                path shouldBe "/nav/pensjon/prognose/v1"
                getHeader("Authorization") shouldBe "Bearer token123"
                getHeader(CustomHttpHeaders.CALL_ID) shouldBe "call-123"
            }
        }
    }

    "2) getResponse returns fallback when body is empty (204)" {
        server?.enqueue(MockResponse().setResponseCode(204)) // Mono.empty -> block() returns null

        Arrange.webClientContextRunner().run {
            // Fallback is constructed as HentPrognoseResponseDto(request.sisteTpnr, tpOrdning.tpNr)
            client(context = it).getPrognose(request, tpDto).tpnr shouldBe "3010"
        }
    }
}) {
    companion object {
        @Language("JSON")
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

