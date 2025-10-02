package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SPKMapper.PROVIDER_FULLT_NAVN
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.AarsakIngenUtbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Delytelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.InkludertOrdning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SPKSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Utbetaling
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

class SPKTjenestepensjonClientTest : FunSpec({

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
        baseUrl = server.url("/").toString()
    }

    afterSpec {
        server?.shutdown()
    }

    test("send request og les respons med tjenestepensjon fra spk") {
        val contextRunner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebClientAutoConfiguration::class.java))

        val tpNummer = "3010"
        val mockResponse = spkSimulerTjenestepensjonResponse()

        server!!.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(mockResponse))
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val sporingslogg = mockk<SporingsloggService>()
            every { sporingslogg.logUtgaaendeRequest(Organisasjoner.SPK, any<Pid>(), any<String>()) } just runs

            val sammenligner = mockk<SammenlignAFPService>()
            every { sammenligner.sammenlignOgLoggAfp(any(), any()) } just Runs

            val spkClient = SPKTjenestepensjonClientFra2025(
                baseUrl = baseUrl!!,
                retryAttempts = "0",
                webClientBuilder = webClientBuilder,
                sammenligner = sammenligner,
                traceAid = mockk(relaxed = true),
                sporingslogg = sporingslogg,
            )

            val response: Result<SimulertTjenestepensjon> =
                spkClient.simuler(dummyRequest("1963-02-05", brukerBaOmAfp = true), tpNummer)

            response.isSuccess.shouldBeTrue()
            val tjenestepensjon = response.getOrNull().shouldNotBeNull()
            tjenestepensjon.tpLeverandoer shouldBe PROVIDER_FULLT_NAVN
            tjenestepensjon.ordningsListe.size shouldBe 1
            tjenestepensjon.ordningsListe[0].tpNummer shouldBe tpNummer
            tjenestepensjon.utbetalingsperioder.size shouldBe 5

            tjenestepensjon.utbetalingsperioder[0].fom shouldBe mockResponse.utbetalingListe[0].fraOgMedDato
            tjenestepensjon.utbetalingsperioder[0].maanedligBelop shouldBe mockResponse.utbetalingListe[0].delytelseListe[0].maanedligBelop
            tjenestepensjon.utbetalingsperioder[0].ytelseType shouldBe mockResponse.utbetalingListe[0].delytelseListe[0].ytelseType

            tjenestepensjon.utbetalingsperioder[1].fom shouldBe mockResponse.utbetalingListe[1].fraOgMedDato
            tjenestepensjon.utbetalingsperioder[1].maanedligBelop shouldBe mockResponse.utbetalingListe[1].delytelseListe[0].maanedligBelop
            tjenestepensjon.utbetalingsperioder[1].ytelseType shouldBe mockResponse.utbetalingListe[1].delytelseListe[0].ytelseType

            tjenestepensjon.utbetalingsperioder[2].fom shouldBe mockResponse.utbetalingListe[2].fraOgMedDato
            tjenestepensjon.utbetalingsperioder[2].maanedligBelop shouldBe mockResponse.utbetalingListe[2].delytelseListe[0].maanedligBelop
            tjenestepensjon.utbetalingsperioder[2].ytelseType shouldBe mockResponse.utbetalingListe[2].delytelseListe[0].ytelseType

            tjenestepensjon.utbetalingsperioder[3].fom shouldBe mockResponse.utbetalingListe[3].fraOgMedDato
            tjenestepensjon.utbetalingsperioder[3].maanedligBelop shouldBe mockResponse.utbetalingListe[3].delytelseListe[0].maanedligBelop
            tjenestepensjon.utbetalingsperioder[3].ytelseType shouldBe mockResponse.utbetalingListe[3].delytelseListe[0].ytelseType

            tjenestepensjon.utbetalingsperioder[4].fom shouldBe mockResponse.utbetalingListe[4].fraOgMedDato
            tjenestepensjon.utbetalingsperioder[4].maanedligBelop shouldBe mockResponse.utbetalingListe[4].delytelseListe[0].maanedligBelop
            tjenestepensjon.utbetalingsperioder[4].ytelseType shouldBe mockResponse.utbetalingListe[4].delytelseListe[0].ytelseType

            tjenestepensjon.aarsakIngenUtbetaling.size shouldBe 1
            tjenestepensjon.aarsakIngenUtbetaling[0].contains(
                mockResponse.aarsakIngenUtbetaling[0].ytelseType
            ).shouldBeTrue()

            val recorded = server!!.takeRequest()
            recorded.path!!.startsWith("$SIMULER_PATH/$tpNummer").shouldBeTrue()
        }
    }

    test("send request og faa error fra spk") {
        val contextRunner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebClientAutoConfiguration::class.java))

        server!!.enqueue(MockResponse().setResponseCode(500))

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val sporingslogg = mockk<SporingsloggService>()
            every { sporingslogg.logUtgaaendeRequest(Organisasjoner.SPK, any<Pid>(), any<String>()) } just runs

            val sammenligner = mockk<SammenlignAFPService>()
            every { sammenligner.sammenlignOgLoggAfp(any(), any()) } just Runs

            val spkClient = SPKTjenestepensjonClientFra2025(
                baseUrl = baseUrl!!,
                retryAttempts = "0",
                webClientBuilder = webClientBuilder,
                sammenligner = sammenligner,
                traceAid = mockk(relaxed = true),
                sporingslogg = sporingslogg,
            )

            // unique request to avoid cache hit
            val response: Result<SimulertTjenestepensjon> =
                spkClient.simuler(dummyRequest("1963-02-06", brukerBaOmAfp = true), "3010")

            response.isFailure.shouldBeTrue()
            (response.exceptionOrNull() is TjenestepensjonSimuleringException).shouldBeTrue()

            val recorded = server.takeRequest()
            recorded.path!!.startsWith(SIMULER_PATH).shouldBeTrue()
        }
    }
}) {
    private companion object {
        private const val SIMULER_PATH = "/nav/v2/tjenestepensjon/simuler"

        fun spkSimulerTjenestepensjonResponse() = SPKSimulerTjenestepensjonResponse(
            inkludertOrdningListe = listOf(InkludertOrdning("3010")),
            utbetalingListe = listOf(
                Utbetaling(LocalDate.parse("2025-03-01"), listOf(Delytelse("OAFP", 1))),
                Utbetaling(LocalDate.parse("2025-03-01"), listOf(Delytelse("PAASLAG", 2))),
                Utbetaling(LocalDate.parse("2025-03-01"), listOf(Delytelse("APOF2020", 3))),
                Utbetaling(LocalDate.parse("2025-03-01"), listOf(Delytelse("OT6370", 4))),
                Utbetaling(LocalDate.parse("2025-03-01"), listOf(Delytelse("AFP", 5))),
            ),
            aarsakIngenUtbetaling = listOf(
                AarsakIngenUtbetaling("IKKE_STOETTET", "Ikke stoettet", "SAERALDERSPAASLAG"),
            )
        )
    }
}
