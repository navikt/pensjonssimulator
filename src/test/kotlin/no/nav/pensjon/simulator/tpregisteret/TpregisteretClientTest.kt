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

    context("findAlleTpForhold") {

        test("should return list of TpForhold when response is OK") {
            arrangeAlleTpForholdResponse(
                server,
                """
                {
                    "fnr": "12345678910",
                    "forhold": [
                        {
                            "tpNr": "3010",
                            "tpOrdningNavn": "Statens pensjonskasse",
                            "datoSistOpptjening": "2024-01-15"
                        }
                    ]
                }
                """
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(context = it).findAlleTpForhold(pid)

                result.size shouldBe 1
                result[0].tpNr shouldBe "3010"
                result[0].navn shouldBe "Statens pensjonskasse"
                result[0].datoSistOpptjening shouldBe java.time.LocalDate.of(2024, 1, 15)
            }
        }

        test("should return multiple TpForhold when response has multiple forhold") {
            arrangeAlleTpForholdResponse(
                server,
                """
                {
                    "fnr": "12345678910",
                    "forhold": [
                        {
                            "tpNr": "3010",
                            "tpOrdningNavn": "Statens pensjonskasse",
                            "datoSistOpptjening": "2024-01-15"
                        },
                        {
                            "tpNr": "3020",
                            "tpOrdningNavn": "KLP",
                            "datoSistOpptjening": "2023-06-30"
                        },
                        {
                            "tpNr": "3030",
                            "tpOrdningNavn": "Oslo Pensjonsforsikring",
                            "datoSistOpptjening": null
                        }
                    ]
                }
                """
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(context = it).findAlleTpForhold(pid)

                result.size shouldBe 3
                result[0].tpNr shouldBe "3010"
                result[0].navn shouldBe "Statens pensjonskasse"
                result[1].tpNr shouldBe "3020"
                result[1].navn shouldBe "KLP"
                result[2].tpNr shouldBe "3030"
                result[2].navn shouldBe "Oslo Pensjonsforsikring"
                result[2].datoSistOpptjening shouldBe null
            }
        }

        test("should return empty list when response has empty forhold") {
            arrangeAlleTpForholdResponse(
                server,
                """
                {
                    "fnr": "12345678910",
                    "forhold": []
                }
                """
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(context = it).findAlleTpForhold(pid)

                result.size shouldBe 0
            }
        }

        test("should throw exception when response is NOT_FOUND") {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                try {
                    client(context = it).findAlleTpForhold(pid)
                    throw AssertionError("Expected exception to be thrown")
                } catch (e: Exception) {
                    // EgressException is thrown due to WebClientBuilderConfiguration filter
                    e.message?.contains("404") shouldBe true
                }
            }
        }

        test("should use tpNr as navn when tpOrdningNavn is null") {
            arrangeAlleTpForholdResponse(
                server,
                """
                {
                    "fnr": "12345678910",
                    "forhold": [
                        {
                            "tpNr": "3010",
                            "tpOrdningNavn": null,
                            "datoSistOpptjening": "2024-01-15"
                        }
                    ]
                }
                """
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(context = it).findAlleTpForhold(pid)

                result.size shouldBe 1
                result[0].tpNr shouldBe "3010"
                result[0].navn shouldBe "3010"
            }
        }

        test("should include fnr header in request") {
            arrangeAlleTpForholdResponse(
                server,
                """
                {
                    "fnr": "12345678910",
                    "forhold": []
                }
                """
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(context = it).findAlleTpForhold(pid)
                val request = server?.takeRequest()

                request?.getHeader("fnr") shouldBe "12345678910"
            }
        }

        test("should throw exception when response is error status") {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                try {
                    client(context = it).findAlleTpForhold(pid)
                    throw AssertionError("Expected exception to be thrown")
                } catch (e: Exception) {
                    // Exception is thrown due to error response
                    (e is RuntimeException) shouldBe true
                }
            }
        }

        test("should throw exception when response is BAD_REQUEST") {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(HttpStatus.BAD_REQUEST.value())
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                try {
                    client(context = it).findAlleTpForhold(pid)
                    throw AssertionError("Expected exception to be thrown")
                } catch (e: Exception) {
                    // EgressException is thrown due to WebClientBuilderConfiguration filter
                    e.message?.contains("400") shouldBe true
                }
            }
        }
    }

    context("findTssId") {

        test("should return tssId when found") {
            arrangeTssIdResponse(server, "12345")

            Arrange.webClientContextRunner().run {
                val result = client(context = it).findTssId("3010")

                result shouldBe "12345"
            }
        }

        test("should throw exception when NOT_FOUND due to filter") {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(HttpStatus.NOT_FOUND.value())
            )

            Arrange.webClientContextRunner().run {
                try {
                    client(context = it).findTssId("9999")
                    throw AssertionError("Expected exception to be thrown")
                } catch (e: Exception) {
                    // EgressException is thrown due to WebClientBuilderConfiguration filter
                    // The original code catches WebClientResponseException.NotFound,
                    // but the filter converts 404 to EgressException first
                    e.message?.contains("404") shouldBe true
                }
            }
        }

        test("should call correct endpoint with tpId") {
            arrangeTssIdResponse(server, "67890")

            Arrange.webClientContextRunner().run {
                client(context = it).findTssId("3010")
                val request = server?.takeRequest()

                request?.path shouldBe "/api/tpconfig/tssnr/3010"
            }
        }

        test("should return different tssIds for different tpIds") {
            arrangeTssIdResponse(server, "11111")

            Arrange.webClientContextRunner().run {
                val result1 = client(context = it).findTssId("3010")
                result1 shouldBe "11111"
            }

            arrangeTssIdResponse(server, "22222")

            Arrange.webClientContextRunner().run {
                val result2 = client(context = it).findTssId("3020")
                result2 shouldBe "22222"
            }
        }

        test("should return null for empty response body") {
            arrangeTssIdResponse(server, "")

            Arrange.webClientContextRunner().run {
                val result = client(context = it).findTssId("3010")

                // Empty response body returns null from bodyToMono
                result shouldBe null
            }
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

private fun arrangeAlleTpForholdResponse(server: MockWebServer?, responseBody: String) {
    server?.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody(responseBody)
    )
}

private fun arrangeTssIdResponse(server: MockWebServer?, tssId: String) {
    server?.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody(tssId)
    )
}
