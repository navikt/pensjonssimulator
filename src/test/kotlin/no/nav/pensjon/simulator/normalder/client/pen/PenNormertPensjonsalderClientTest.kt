package no.nav.pensjon.simulator.normalder.client.pen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.VerdiStatus
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanFactory
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class PenNormertPensjonsalderClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory, cacheManager: CaffeineCacheManager = CaffeineCacheManager()) =
        PenNormertPensjonsalderClient(
            baseUrl = baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            cacheManager = cacheManager,
            traceAid = mockk<TraceAid>(relaxed = true)
        )

    beforeSpec {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server!!.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    context("fetchNormalderListe") {

        test("returnerer liste med Aldersgrenser fra respons") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody(
                        """{
                            "normertPensjonsalderListe": [
                                {
                                    "aarskull": 1963,
                                    "aar": 67,
                                    "maaned": 0,
                                    "nedreAar": 62,
                                    "nedreMaaned": 0,
                                    "oevreAar": 75,
                                    "oevreMaaned": 0,
                                    "type": "FAST"
                                }
                            ]
                        }"""
                    )
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchNormalderListe()

                result.size shouldBe 1
                result[0].aarskull shouldBe 1963
                result[0].normalder shouldBe Alder(67, 0)
                result[0].nedreAlder shouldBe Alder(62, 0)
                result[0].oevreAlder shouldBe Alder(75, 0)
                result[0].verdiStatus shouldBe VerdiStatus.FAST
            }
        }

        test("returnerer flere Aldersgrenser fra respons") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody(
                        """{
                            "normertPensjonsalderListe": [
                                {
                                    "aarskull": 1963,
                                    "aar": 67,
                                    "maaned": 0,
                                    "nedreAar": 62,
                                    "nedreMaaned": 0,
                                    "oevreAar": 75,
                                    "oevreMaaned": 0,
                                    "type": "FAST"
                                },
                                {
                                    "aarskull": 1975,
                                    "aar": 67,
                                    "maaned": 6,
                                    "nedreAar": 62,
                                    "nedreMaaned": 6,
                                    "oevreAar": 75,
                                    "oevreMaaned": 6,
                                    "type": "PROGNOSE"
                                }
                            ]
                        }"""
                    )
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchNormalderListe()

                result.size shouldBe 2
                result[0].aarskull shouldBe 1963
                result[0].verdiStatus shouldBe VerdiStatus.FAST
                result[1].aarskull shouldBe 1975
                result[1].verdiStatus shouldBe VerdiStatus.PROGNOSE
            }
        }

        test("returnerer tom liste fra respons med tom liste") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"normertPensjonsalderListe": []}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchNormalderListe()

                result.size shouldBe 0
            }
        }

        test("returnerer tom liste fra null respons") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchNormalderListe()

                result.size shouldBe 0
            }
        }

        test("kaller korrekt endpoint") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"normertPensjonsalderListe": []}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(it).fetchNormalderListe()

                val request = server?.takeRequest()
                request?.path shouldBe "/api/normertpensjonsalder"
            }
        }

        test("bruker GET metode") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"normertPensjonsalderListe": []}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(it).fetchNormalderListe()

                val request = server?.takeRequest()
                request?.method shouldBe "GET"
            }
        }

        test("kaster EgressException ved server error") {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody("Internal Server Error")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                shouldThrow<EgressException> {
                    client(it).fetchNormalderListe()
                }
            }
        }

        test("kaster EgressException ved bad request") {
            server?.enqueue(
                MockResponse()
                    .setResponseCode(HttpStatus.BAD_REQUEST.value())
                    .setBody("Bad Request")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                shouldThrow<EgressException> {
                    client(it).fetchNormalderListe()
                }
            }
        }

        test("kaster RuntimeException når respons har feilmelding") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody(
                        """{
                            "normertPensjonsalderListe": null,
                            "message": "Feil ved henting av data",
                            "aarskull": 1963
                        }"""
                    )
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                shouldThrow<RuntimeException> {
                    client(it).fetchNormalderListe()
                }
            }
        }
    }

    context("caching") {

        test("cacher resultat for fetchNormalderListe") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody(
                        """{
                            "normertPensjonsalderListe": [
                                {
                                    "aarskull": 1963,
                                    "aar": 67,
                                    "maaned": 0,
                                    "nedreAar": 62,
                                    "nedreMaaned": 0,
                                    "oevreAar": 75,
                                    "oevreMaaned": 0,
                                    "type": "FAST"
                                }
                            ]
                        }"""
                    )
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val cacheManager = CaffeineCacheManager()
                val clientInstance = client(it, cacheManager)

                val requestCountBefore = server?.requestCount ?: 0

                // First call
                val result1 = clientInstance.fetchNormalderListe()
                result1.size shouldBe 1
                result1[0].aarskull shouldBe 1963

                // Second call - should use cache
                val result2 = clientInstance.fetchNormalderListe()
                result2.size shouldBe 1
                result2[0].aarskull shouldBe 1963

                // Only one request should have been made
                (server?.requestCount ?: 0) - requestCountBefore shouldBe 1
            }
        }
    }
})
