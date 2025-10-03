package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.SammenlignAFPService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025ServiceUnitTest.Companion.dummyRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.PROVIDER_FULLT_NAVN
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.ArsakIngenUtbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Utbetaling
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class KLPTjenestepensjonClientFunSpec : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            TestingAuthenticationToken(
                "TEST_USER",
                Jwt("j.w.t", null, null, mapOf("k" to "v"), mapOf("k" to "v"))
            ),
            EgressTokenSuppliersByService(mapOf())
        )

        server = MockWebServer().also { it.start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("send request og les respons med tjenestepensjon fra klp") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        val tpNummer = "995566"
        val mockResponse = klpSimulerTjenestepensjonResponse()

        server!!.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(mockResponse))
        )
        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val sporingslogg = mockk<SporingsloggService>()
            every { sporingslogg.logUtgaaendeRequest(Organisasjoner.KLP, any<Pid>(), any<String>()) } just runs

            val sammenligner = mockk<SammenlignAFPService>()
            every { sammenligner.sammenlignOgLoggAfp(any(), any()) } just Runs
            val klpClient = KlpTjenestepensjonClientFra2025(
                baseUrl!!,
                retryAttempts = "0",
                webClientBuilder,
                traceAid = mockk(relaxed = true),
                sporingslogg = sporingslogg,
                sammenligner = sammenligner,
            )

            val response: Result<SimulertTjenestepensjon> =
                klpClient.simuler(dummyRequest("1963-02-05", brukerBaOmAfp = true), tpNummer)

            response.isSuccess.shouldBeTrue()
            val tjenestepensjon = response.getOrNull().shouldNotBeNull()
            tjenestepensjon.tpLeverandoer shouldBe PROVIDER_FULLT_NAVN
            tjenestepensjon.ordningsListe shouldHaveSize 1
            tjenestepensjon.ordningsListe[0].tpNummer shouldBe tpNummer
            tjenestepensjon.utbetalingsperioder shouldHaveSize 4

            with(tjenestepensjon.utbetalingsperioder[0]) {
                fom shouldBe mockResponse.utbetalingsListe[0].fraOgMedDato
                maanedligBelop shouldBe mockResponse.utbetalingsListe[0].manedligUtbetaling
                ytelseType shouldBe mockResponse.utbetalingsListe[0].ytelseType
            }

            with(tjenestepensjon.utbetalingsperioder[1]){
                fom shouldBe mockResponse.utbetalingsListe[1].fraOgMedDato
                maanedligBelop shouldBe mockResponse.utbetalingsListe[1].manedligUtbetaling
                ytelseType shouldBe mockResponse.utbetalingsListe[1].ytelseType
            }

            with(tjenestepensjon.utbetalingsperioder[2]){
                fom shouldBe mockResponse.utbetalingsListe[2].fraOgMedDato
                maanedligBelop shouldBe mockResponse.utbetalingsListe[2].manedligUtbetaling
                ytelseType shouldBe mockResponse.utbetalingsListe[2].ytelseType
            }

            with(tjenestepensjon.utbetalingsperioder[3]){
                fom shouldBe mockResponse.utbetalingsListe[3].fraOgMedDato
                maanedligBelop shouldBe mockResponse.utbetalingsListe[3].manedligUtbetaling
                ytelseType shouldBe mockResponse.utbetalingsListe[3].ytelseType
            }

            tjenestepensjon.aarsakIngenUtbetaling shouldHaveSize 1
            tjenestepensjon.aarsakIngenUtbetaling.first().contains(
                mockResponse.arsakIngenUtbetaling.first().ytelseType
            ).shouldBeTrue()

            val recorded = server.takeRequest()
            recorded.path!!.startsWith("$SIMULER_PATH/$tpNummer").shouldBeTrue()
        }

    }

    test("send request og faa error fra klp") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(MockResponse().setResponseCode(500))

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val sporingslogg = mockk<SporingsloggService>()
            every { sporingslogg.logUtgaaendeRequest(Organisasjoner.KLP, any<Pid>(), any<String>()) } just runs

            val sammenligner = mockk<SammenlignAFPService>()
            every { sammenligner.sammenlignOgLoggAfp(any(), any()) } just Runs

            val klpClient = KlpTjenestepensjonClientFra2025(
                baseUrl!!,
                retryAttempts = "0",
                webClientBuilder,
                traceAid = mockk(relaxed = true),
                sporingslogg = sporingslogg,
                sammenligner = sammenligner
            )
            val response: Result<SimulertTjenestepensjon> =
                klpClient.simuler(dummyRequest("1963-02-06", brukerBaOmAfp = true), "3100")

            response.isFailure.shouldBeTrue()
            (response.exceptionOrNull() is TjenestepensjonSimuleringException).shouldBeTrue()

            val recorded = server.takeRequest()
            recorded.path!!.startsWith(SIMULER_PATH).shouldBeTrue()
        }
    }

    test("ikke send request og returner mock i dev-gcp fra klp") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )
        val request = dummyRequest("1963-02-07", brukerBaOmAfp = true)
        val tpNummer = "3100"

        val mockExpectedResponse = KlpTjenestepensjonClientFra2025.provideMockResponse(request)

        server!!.enqueue(MockResponse().setResponseCode(500))

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val sporingslogg = mockk<SporingsloggService>()
            every { sporingslogg.logUtgaaendeRequest(Organisasjoner.KLP, any<Pid>(), any<String>()) } just runs

            val sammenligner = mockk<SammenlignAFPService>()
            every { sammenligner.sammenlignOgLoggAfp(any(), any()) } just Runs
            val klpClient = KlpTjenestepensjonClientFra2025(
                baseUrl!!,
                retryAttempts = "0",
                webClientBuilder,
                traceAid = mockk(relaxed = true),
                sporingslogg = sporingslogg,
                sammenligner = sammenligner,
                clusterName = { "dev-gcp" }
            )

            val response: Result<SimulertTjenestepensjon> = klpClient.simuler(request, tpNummer)

            response.isSuccess.shouldBeTrue()
            val tjenestepensjon = response.getOrNull().shouldNotBeNull()
            tjenestepensjon.tpLeverandoer shouldBe PROVIDER_FULLT_NAVN
            tjenestepensjon.ordningsListe shouldHaveSize 1
            tjenestepensjon.ordningsListe[0].tpNummer shouldBe tpNummer
            tjenestepensjon.utbetalingsperioder shouldHaveSize 3

            with(tjenestepensjon.utbetalingsperioder[0]) {
                fom shouldBe mockExpectedResponse.utbetalingsListe[0].fraOgMedDato
                maanedligBelop shouldBe mockExpectedResponse.utbetalingsListe[0].manedligUtbetaling
                ytelseType shouldBe mockExpectedResponse.utbetalingsListe[0].ytelseType
            }

            with(tjenestepensjon.utbetalingsperioder[1]){
                fom shouldBe mockExpectedResponse.utbetalingsListe[1].fraOgMedDato
                maanedligBelop shouldBe mockExpectedResponse.utbetalingsListe[1].manedligUtbetaling
                ytelseType shouldBe mockExpectedResponse.utbetalingsListe[1].ytelseType
            }

            with(tjenestepensjon.utbetalingsperioder[2]){
                fom shouldBe mockExpectedResponse.utbetalingsListe[2].fraOgMedDato
                maanedligBelop shouldBe mockExpectedResponse.utbetalingsListe[2].manedligUtbetaling
                ytelseType shouldBe mockExpectedResponse.utbetalingsListe[2].ytelseType
            }
        }
    }
}) {

    private companion object {
        private const val SIMULER_PATH = "/api/oftp/simulering"
        fun klpSimulerTjenestepensjonResponse() = KlpSimulerTjenestepensjonResponse(
            inkludertOrdningListe = listOf(InkludertOrdning("995566")),
            utbetalingsListe = listOf(
                Utbetaling(LocalDate.parse("2025-03-01"), 1, 12, "OAFP"),
                Utbetaling(LocalDate.parse("2025-03-01"), 2, 24, "PAASLAG"),
                Utbetaling(LocalDate.parse("2025-03-01"), 3, 36, "APOF2020"),
                Utbetaling(LocalDate.parse("2025-03-01"), 4, 48, "OT6370")
            ),
            arsakIngenUtbetaling = listOf(ArsakIngenUtbetaling("IKKE_STOETTET", "Ikke stoettet", "SAERALDERSPAASLAG")),
            betingetTjenestepensjonErInkludert = false,
        )
    }
}
