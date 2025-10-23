package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

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
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import no.nav.pensjon.simulator.testutil.arrangeResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.ArsakIngenUtbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Utbetaling
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class KlpTjenestepensjonClientFra2025Test : FunSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    val sporingslogg = mockk<SporingsloggService> {
        every { logUtgaaendeRequest(Organisasjoner.KLP, any<Pid>(), any<String>()) } just runs
    }

    val sammenligner = mockk<SammenlignAFPService> {
        every { sammenlignOgLoggAfp(any(), any()) } just runs
    }

    fun client(context: BeanFactory, retryAttempts: Int = 0, isDevelopment: Boolean = false) =
        KlpTjenestepensjonClientFra2025(
            baseUrl!!,
            retryAttempts = retryAttempts.toString(),
            webClientBuilder = context.getBean(WebClient.Builder::class.java),
            traceAid = mockk<TraceAid>(relaxed = true),
            sporingslogg,
            sammenligner,
            isDevelopment = { isDevelopment }
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

    test("send request og les respons med tjenestepensjon fra KLP") {
        val tpNummer = "995566"
        val serverResponse = simulerTjenestepensjonResponse()
        server?.arrangeOkJsonResponse(body = objectMapper.writeValueAsString(serverResponse))

        Arrange.webClientContextRunner().run {
            val receivedResponse = client(context = it, retryAttempts = 1).simuler(
                spec = spec(foedselsdato = LocalDate.of(1963, 2, 5)),
                tpNummer
            )

            receivedResponse.isSuccess shouldBe true
            with(receivedResponse.getOrNull().shouldNotBeNull()) {
                tpLeverandoer shouldBe "Kommunal Landspensjonskasse"
                ordningsListe shouldHaveSize 1
                ordningsListe[0].tpNummer shouldBe tpNummer
                utbetalingsperioder shouldHaveSize 4
                assertUtbetalingsperioder(actual = utbetalingsperioder, expected = serverResponse.utbetalingsListe)
                aarsakIngenUtbetaling shouldHaveSize 1
                aarsakIngenUtbetaling.first().contains(
                    serverResponse.arsakIngenUtbetaling.first().ytelseType
                ) shouldBe true
            }
            server?.takeRequest()?.path?.startsWith("$SIMULER_PATH/$tpNummer") shouldBe true
        }
    }

    test("send request og få error fra KLP") {
        server?.arrangeResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, body = "feil")

        Arrange.webClientContextRunner().run {
            val receivedResponse = client(context = it).simuler(
                spec = spec(foedselsdato = LocalDate.of(1963, 2, 6)),
                tpNummer = "3100"
            )

            receivedResponse.isFailure shouldBe true
            receivedResponse.exceptionOrNull().shouldBeInstanceOf<TjenestepensjonSimuleringException>()
            server?.takeRequest()?.path?.startsWith(SIMULER_PATH) shouldBe true
        }
    }

    test("ikke send request og returner mock i development fra KLP") {
        val tpNummer = "3100"
        val spec = spec(foedselsdato = LocalDate.of(1963, 2, 7))

        Arrange.webClientContextRunner().run {
            val receivedResponse = client(context = it, isDevelopment = true).simuler(spec, tpNummer)

            receivedResponse.isSuccess shouldBe true
            with(receivedResponse.getOrNull().shouldNotBeNull()) {
                tpLeverandoer shouldBe "Kommunal Landspensjonskasse"
                ordningsListe shouldHaveSize 1
                ordningsListe[0].tpNummer shouldBe tpNummer
                utbetalingsperioder shouldHaveSize 3
                assertUtbetalingsperioder(
                    actual = utbetalingsperioder,
                    expected = expectedMockUtbetalinger(spec.uttaksdato)
                )
            }
        }
    }
}) {
    private companion object {
        private const val SIMULER_PATH = "/api/oftp/simulering"

        private fun spec(foedselsdato: LocalDate) =
            SimulerOffentligTjenestepensjonFra2025SpecV1(
                pid = "12345678910",
                foedselsdato,
                uttaksdato = LocalDate.of(2025, 3, 1),
                sisteInntekt = 500000,
                aarIUtlandetEtter16 = 0,
                brukerBaOmAfp = true,
                epsPensjon = false,
                eps2G = false,
                fremtidigeInntekter = emptyList(),
                erApoteker = false
            )

        private fun simulerTjenestepensjonResponse() =
            KlpSimulerTjenestepensjonResponse(
                inkludertOrdningListe = listOf(InkludertOrdning(tpnr = "995566")),
                utbetalingsListe = listOf(
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        manedligUtbetaling = 1,
                        arligUtbetaling = 12,
                        ytelseType = "OAFP"
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        manedligUtbetaling = 2,
                        arligUtbetaling = 24,
                        ytelseType = "PAASLAG"
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        manedligUtbetaling = 3,
                        arligUtbetaling = 36,
                        ytelseType = "APOF2020"
                    ),
                    Utbetaling(
                        fraOgMedDato = LocalDate.of(2025, 3, 1),
                        manedligUtbetaling = 4,
                        arligUtbetaling = 48,
                        ytelseType = "OT6370"
                    )
                ),
                arsakIngenUtbetaling = listOf(
                    ArsakIngenUtbetaling(
                        statusKode = "IKKE_STOETTET",
                        statusBeskrivelse = "Ikke stoettet",
                        ytelseType = "SAERALDERSPAASLAG"
                    )
                ),
                betingetTjenestepensjonErInkludert = false
            )

        private fun expectedMockUtbetalinger(uttaksdato: LocalDate): List<Utbetaling> =
            listOf(
                Utbetaling(
                    fraOgMedDato = uttaksdato,
                    manedligUtbetaling = 3576,
                    arligUtbetaling = 42914,
                    ytelseType = "PAASLAG"
                ),
                Utbetaling(
                    fraOgMedDato = uttaksdato.plusYears(5),
                    manedligUtbetaling = 2232,
                    arligUtbetaling = 26779,
                    ytelseType = "APOF2020"
                ),
                Utbetaling(
                    fraOgMedDato = uttaksdato,
                    manedligUtbetaling = 884,
                    arligUtbetaling = 10609,
                    ytelseType = "BTP"
                ),
            )

        private fun assertUtbetalingsperioder(actual: List<Utbetalingsperiode>, expected: List<Utbetaling>) {
            IntRange(0, expected.size - 1).forEach {
                assertUtbetalingsperiode(actual[it], expected[it])
            }
        }

        private fun assertUtbetalingsperiode(actual: Utbetalingsperiode, expected: Utbetaling) {
            actual.fom shouldBe expected.fraOgMedDato
            actual.maanedligBelop shouldBe expected.manedligUtbetaling
            actual.ytelseType shouldBe expected.ytelseType
        }
    }
}
