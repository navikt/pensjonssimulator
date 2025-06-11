package no.nav.pensjon.simulator.opptjening

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

class OpptjeningClientTest : FunSpec({

    var webServer = MockWebServer()
    var baseUrl: String?
    lateinit var client: OpptjeningClient

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication =
            EnrichedAuthentication(
                initialAuth = TestingAuthenticationToken("TEST_USER", jwt),
                egressTokenSuppliersByService = EgressTokenSuppliersByService(mapOf())
            )

        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(3))
            .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(3)) }

        webServer = MockWebServer().also {
            it.start()
            baseUrl = "http://localhost:${it.port}"

            val webClient = WebClient.builder()
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build()

            client = OpptjeningClient(
                baseUrl,
                retryAttempts = "1",
                webClientBuilder = webClient.mutate(),
                traceAid = mockk(relaxed = true),
                time = { LocalDate.of(2024, 6, 15) } // "dagens dato"
            )
        }
    }

    afterSpec {
        webServer.shutdown()
    }

    test("skal returnere Inntekt fra POPP") {
        val responseBody = """
            {
              "opptjeningsGrunnlag": {
                "inntektListe": [{
                  "inntektType": "SUM_PI",
                  "inntektAr": 2025,
                  "belop": 123456
                }]
              }
            }
        """.trimIndent()

        webServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )

        client.hentSisteLignetInntekt(Pid("12345678910")) shouldBe Inntekt(123456, LocalDate.of(2025, 1, 1))
    }

    test("skal returnere 0 inntekt med fom lik 1. januar inneværende år") {
        webServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("")
        )

        client.hentSisteLignetInntekt(Pid("12345678910")) shouldBe Inntekt(
            aarligBeloep = 0,
            fom = LocalDate.of(2024, 1, 1) // "inneværende år" er 2024 siden "dagens dato" er 2024-06-15
        )
    }

    test("skal kaste EgressException ved 500 Internal Server Error") {
        webServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        shouldThrow<EgressException> {
            client.hentSisteLignetInntekt(Pid("12345678910"))
        }.message shouldBe "Internal Server Error"
    }

    test("skal kaste EgressException ved timeout").config(timeout = 10.seconds) {
        webServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE) // Simulate timeout
        )

        shouldThrow<EgressException> {
            client.hentSisteLignetInntekt(Pid("12345678910"))
        }.cause.shouldBeInstanceOf<WebClientRequestException>()
    }
})
