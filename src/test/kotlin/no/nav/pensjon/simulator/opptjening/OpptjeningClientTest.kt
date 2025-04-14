package no.nav.pensjon.simulator.opptjening

import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.mockito.Mockito.mock
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

class OpptjeningClientTest : FunSpec({

    var mockWebServer = MockWebServer()
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
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(3))
            }


        mockWebServer = MockWebServer().also {
            it.start()
            baseUrl = "http://localhost:${it.port}"

            val webClient = WebClient.builder()
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl!!)
                .build()

            client = OpptjeningClient(baseUrl!!, "1", webClient.mutate(), mock(TraceAid::class.java))
        }
    }

    afterSpec {
        mockWebServer.shutdown()
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

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )

        val result = client.hentSisteLignetInntekt(Pid("12345678910"))
        result shouldBe Inntekt(123456, LocalDate.of(2025, 1, 1))
    }

    test("skal kaste EgressException ved 500 Internal Server Error") {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        val exception = shouldThrow<EgressException> {
            client.hentSisteLignetInntekt(Pid("12345678910"))
        }

        exception.message shouldBe "Internal Server Error"
    }

    test("skal kaste EgressException ved timeout").config(timeout = 10.seconds) {
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE) // Simulate timeout
        )

        val exception = shouldThrow<EgressException> {
            client.hentSisteLignetInntekt(Pid("12345678910"))
        }

        exception.cause.shouldBeInstanceOf<WebClientRequestException>()
    }

})
