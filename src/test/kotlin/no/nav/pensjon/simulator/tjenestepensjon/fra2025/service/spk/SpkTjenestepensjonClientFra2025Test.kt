package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025ServiceUnitTest.Companion.dummyRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanFactory
import org.springframework.http.HttpStatus
import java.time.LocalDate

class SpkTjenestepensjonClientFra2025Test : FunSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    val sporingslogg = mockk<SporingsloggService> {
        every { logUtgaaendeRequest(Organisasjoner.SPK, any<Pid>(), any<String>()) } just runs
    }

    val sammenligner = mockk<SammenlignAFPService> {
        every { sammenlignOgLoggAfp(any(), any()) } just runs
    }

    fun client(context: BeanFactory) =
        SpkTjenestepensjonClientFra2025(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            traceAid = mockk(relaxed = true),
            sporingslogg,
            sammenligner
        )

    beforeSpec {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("send request og les respons med tjenestepensjon fra SPK") {
        val tpNummer = "3010"
        val mockResponse = simulerTjenestepensjonResponse()

        server!!.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(mockResponse))
        )

        Arrange.webClientContextRunner().run {
            val result: Result<SimulertTjenestepensjon> =
                client(context = it).simuler(
                    spec = dummyRequest(foedselsdato = "1963-02-05", afpErForespurt = true),
                    tpNummer
                )

            result.isSuccess shouldBe true
            with(result.getOrNull().shouldNotBeNull()) {
                tpLeverandoer shouldBe "Statens Pensjonskasse"
                ordningsListe shouldHaveSize 1
                ordningsListe[0].tpNummer shouldBe tpNummer
                utbetalingsperioder shouldHaveSize 5
                assertUtbetalingsperioder(actual = utbetalingsperioder, expected = mockResponse.utbetalingListe)
                aarsakIngenUtbetaling shouldHaveSize 1
                aarsakIngenUtbetaling[0].contains(mockResponse.aarsakIngenUtbetaling[0].ytelseType) shouldBe true
            }
            server.takeRequest().path?.startsWith("$SIMULER_PATH/$tpNummer") shouldBe true
        }
    }

    test("send request og få error fra SPK") {
        server?.arrangeResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, body = "feil")

        Arrange.webClientContextRunner().run {
            // unique request to avoid cache hit
            val response: Result<SimulertTjenestepensjon> =
                client(context = it).simuler(dummyRequest(foedselsdato = "1963-02-06", afpErForespurt = true), "3010")

            response.isFailure shouldBe true
            response.exceptionOrNull().shouldBeInstanceOf<TjenestepensjonSimuleringException>()
            server?.takeRequest()?.path?.startsWith(SIMULER_PATH) shouldBe true
        }
    }
}) {
    private companion object {
        private const val SIMULER_PATH = "/nav/v2/tjenestepensjon/simuler"

        fun simulerTjenestepensjonResponse() =
            SpkSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "3010")),
                utbetalingListe = listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        delytelseListe = listOf(Delytelse(ytelseType = "OAFP", maanedligBelop = 1))
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        delytelseListe = listOf(Delytelse(ytelseType = "PAASLAG", maanedligBelop = 2))
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        delytelseListe = listOf(Delytelse(ytelseType = "APOF2020", maanedligBelop = 3))
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        delytelseListe = listOf(Delytelse(ytelseType = "OT6370", maanedligBelop = 4))
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        delytelseListe = listOf(Delytelse(ytelseType = "AFP", maanedligBelop = 5))
                    ),
                ),
                aarsakIngenUtbetaling = listOf(
                    AarsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    ),
                )
            )

        private fun assertUtbetalingsperioder(actual: List<Utbetalingsperiode>, expected: List<Utbetaling>) {
            IntRange(0, expected.size - 1).forEach {
                assertUtbetalingsperiode(actual[it], expected[it])
            }
        }

        private fun assertUtbetalingsperiode(actual: Utbetalingsperiode, expected: Utbetaling) {
            actual.fom shouldBe expected.fraOgMedDato
            actual.maanedligBelop shouldBe expected.delytelseListe[0].maanedligBelop
            actual.ytelseType shouldBe expected.delytelseListe[0].ytelseType
        }
    }
}
