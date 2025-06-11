package no.nav.pensjon.simulator.tpregisteret

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import no.nav.pensjon.simulator.testutil.TestObjects.organisasjonsnummer
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.reactive.function.client.WebClient

/**
 * NB: These tests work with Mockito, but not with Mockk.
 * Reason: Pid and Organisasjonsnummer are @JvmInline value class (which Mockk does not support)
 */
class TpregisteretClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication =
            EnrichedAuthentication(
                initialAuth = TestingAuthenticationToken("TEST_USER", jwt),
                egressTokenSuppliersByService = EgressTokenSuppliersByService(mapOf())
            )

        server = MockWebServer().also {
            it.start()
            baseUrl = "http://localhost:${it.port}"
        }
    }

    afterSpec {
        server?.shutdown()
    }

    test("should have PID as request body") {
        arrangeForhold(server, harForhold = true)

        arrangeContextRunner().run {
            val client = client(baseUrl, arrangeWebClient(it))
            client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer)
            server?.takeRequest()?.let(::assertBodyIsPid)
        }
    }

    test("should return true when response has forhold as true") {
        arrangeForhold(server, harForhold = true)

        arrangeContextRunner().run {
            val client = client(baseUrl, arrangeWebClient(it))
            client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe true
        }
    }

    test("should return false when response has forhold as false") {
        arrangeForhold(server, harForhold = false)

        arrangeContextRunner().run {
            val client = client(baseUrl, arrangeWebClient(it))
            client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe false
        }
    }

    test("should return false when response is null") {
        arrangeForhold(server, harForhold = null)

        arrangeContextRunner().run {
            val client = client(baseUrl, arrangeWebClient(it))
            client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe false
        }
    }

    test("should return false when tpregisteret has internal server error") {
        arrangeInternalError(server)

        arrangeContextRunner().run {
            val client = client(baseUrl, arrangeWebClient(it))
            client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe false
        }
    }
})

private fun client(baseUrl: String?, webClientBuilder: WebClient.Builder) =
    TpregisteretClient(
        baseUrl = baseUrl ?: throw RuntimeException("baseUrl is null"),
        retryAttempts = "0",
        webClientBuilder,
        traceAid = mock(TraceAid::class.java)
    )

private fun arrangeContextRunner() =
    ApplicationContextRunner().withConfiguration(
        AutoConfigurations.of(WebClientAutoConfiguration::class.java)
    )

private fun arrangeWebClient(context: AssertableApplicationContext): WebClient.Builder =
    context.getBean(WebClient.Builder::class.java)

private fun arrangeForhold(server: MockWebServer?, harForhold: Boolean?) {
    server?.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody("{\"forhold\": $harForhold}")
    )
}

private fun arrangeInternalError(server: MockWebServer?) {
    server?.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
    )
}

private fun assertBodyIsPid(request: RecordedRequest) {
    val body = request.body

    ByteArray(body.size.toInt()).also {
        body.read(it)
        String(it) shouldBe "12345678910"
    }
}
