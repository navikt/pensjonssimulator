package no.nav.pensjon.simulator.tpregisteret

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.organisasjonsnummer
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.beans.factory.BeanFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class TpregisteretClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        TpregisteretClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            traceAid = mockk<TraceAid>(relaxed = true),
        )

    beforeSpec {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("should have PID as request body") {
        arrangeForhold(server, harForhold = true)

        Arrange.webClientContextRunner().run {
            client(context = it).hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer)
            server?.takeRequest()?.let(::assertBodyIsPid)
        }
    }

    test("should return true when response has forhold as true") {
        arrangeForhold(server, harForhold = true)

        Arrange.webClientContextRunner().run {
            client(context = it).hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe true
        }
    }

    test("should return false when response has forhold as false") {
        arrangeForhold(server, harForhold = false)

        Arrange.webClientContextRunner().run {
            client(context = it).hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe false
        }
    }

    test("should return false when response is null") {
        arrangeForhold(server, harForhold = null)

        Arrange.webClientContextRunner().run {
            client(context = it).hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe false
        }
    }

    test("should return false when tpregisteret has internal server error") {
        arrangeInternalError(server)

        Arrange.webClientContextRunner().run {
            client(context = it).hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer) shouldBe false
        }
    }
})

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
