package no.nav.pensjon.simulator.vedtak.client.pen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid
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
import java.time.LocalDate

class PenVedtakClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenVedtakClient(
            baseUrl = baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            cacheManager = CaffeineCacheManager(),
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

    context("tidligsteKapittel20VedtakGjelderFom") {

        test("returnerer dato fra respons") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"dato": "2024-01-15"}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).tidligsteKapittel20VedtakGjelderFom(
                    Pid("10000000001"),
                    SakTypeEnum.ALDER
                )

                result shouldBe LocalDate.of(2024, 1, 15)
            }
        }

        test("returnerer null når dato er null i respons") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"dato": null}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).tidligsteKapittel20VedtakGjelderFom(
                    Pid("10000000002"),
                    SakTypeEnum.ALDER
                )

                result shouldBe null
            }
        }

        test("kaller korrekt endpoint") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"dato": null}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(it).tidligsteKapittel20VedtakGjelderFom(
                    Pid("10000000004"),
                    SakTypeEnum.ALDER
                )

                val request = server?.takeRequest()
                request?.path shouldBe "/api/vedtak/v1/tidligste-kap20-fom"
            }
        }

        test("bruker POST metode") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"dato": null}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(it).tidligsteKapittel20VedtakGjelderFom(
                    Pid("10000000005"),
                    SakTypeEnum.ALDER
                )

                val request = server?.takeRequest()
                request?.method shouldBe "POST"
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
                    client(it).tidligsteKapittel20VedtakGjelderFom(
                        Pid("10000000006"),
                        SakTypeEnum.ALDER
                    )
                }
            }
        }

        test("håndterer ulike saktyper") {
            val sakTypes = listOf(SakTypeEnum.OMSORG, SakTypeEnum.GENRL, SakTypeEnum.GRBL)

            sakTypes.forEachIndexed { index, sakType ->
                server?.enqueue(
                    MockResponse()
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setResponseCode(HttpStatus.OK.value())
                        .setBody("""{"dato": "2024-06-01"}""")
                )

                Arrange.webClientContextRunner().run {
                    Arrange.security()
                    val result = client(it).tidligsteKapittel20VedtakGjelderFom(
                        Pid("1000000010${index}"),
                        sakType
                    )

                    result shouldBe LocalDate.of(2024, 6, 1)
                }
            }
        }
    }

    context("fetchVedtakStatus") {

        test("returnerer VedtakStatus fra respons") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"harGjeldendeVedtak": true, "harGjenlevenderettighet": false}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchVedtakStatus(
                    Pid("20000000001"),
                    LocalDate.of(2024, 1, 1)
                )

                result.harGjeldendeVedtak shouldBe true
                result.harGjenlevenderettighet shouldBe false
            }
        }

        test("returnerer VedtakStatus med begge flagg true") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"harGjeldendeVedtak": true, "harGjenlevenderettighet": true}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchVedtakStatus(
                    Pid("20000000002"),
                    LocalDate.of(2024, 2, 2)
                )

                result.harGjeldendeVedtak shouldBe true
                result.harGjenlevenderettighet shouldBe true
            }
        }

        test("returnerer VedtakStatus med begge flagg false") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"harGjeldendeVedtak": false, "harGjenlevenderettighet": false}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val result = client(it).fetchVedtakStatus(
                    Pid("20000000003"),
                    LocalDate.of(2024, 3, 3)
                )

                result.harGjeldendeVedtak shouldBe false
                result.harGjenlevenderettighet shouldBe false
            }
        }

        test("kaller korrekt endpoint") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"harGjeldendeVedtak": false, "harGjenlevenderettighet": false}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(it).fetchVedtakStatus(
                    Pid("20000000006"),
                    LocalDate.of(2024, 7, 1)
                )

                val request = server?.takeRequest()
                request?.path shouldBe "/api/vedtak/v1/status-for-simulator"
            }
        }

        test("bruker POST metode") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"harGjeldendeVedtak": false, "harGjenlevenderettighet": false}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                client(it).fetchVedtakStatus(
                    Pid("20000000007"),
                    LocalDate.of(2024, 8, 1)
                )

                val request = server?.takeRequest()
                request?.method shouldBe "POST"
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
                    client(it).fetchVedtakStatus(
                        Pid("20000000008"),
                        LocalDate.of(2024, 9, 1)
                    )
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
                    client(it).fetchVedtakStatus(
                        Pid("20000000009"),
                        LocalDate.of(2024, 10, 1)
                    )
                }
            }
        }
    }

    context("caching") {

        test("cacher resultat for tidligsteKapittel20VedtakGjelderFom") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"dato": "2024-01-15"}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val clientInstance = client(it)
                val pid = Pid("30000000001")
                val sakType = SakTypeEnum.BARNEP

                val requestCountBefore = server?.requestCount ?: 0

                // First call
                val result1 = clientInstance.tidligsteKapittel20VedtakGjelderFom(pid, sakType)
                result1 shouldBe LocalDate.of(2024, 1, 15)

                // Second call with same parameters - should use cache
                val result2 = clientInstance.tidligsteKapittel20VedtakGjelderFom(pid, sakType)
                result2 shouldBe LocalDate.of(2024, 1, 15)

                // Only one additional request should have been made
                (server?.requestCount ?: 0) - requestCountBefore shouldBe 1
            }
        }

        test("cacher resultat for fetchVedtakStatus") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value())
                    .setBody("""{"harGjeldendeVedtak": true, "harGjenlevenderettighet": false}""")
            )

            Arrange.webClientContextRunner().run {
                Arrange.security()
                val clientInstance = client(it)
                val pid = Pid("30000000002")
                val uttakFom = LocalDate.of(2030, 12, 31)

                val requestCountBefore = server?.requestCount ?: 0

                // First call
                val result1 = clientInstance.fetchVedtakStatus(pid, uttakFom)
                result1.harGjeldendeVedtak shouldBe true

                // Second call with same parameters - should use cache
                val result2 = clientInstance.fetchVedtakStatus(pid, uttakFom)
                result2.harGjeldendeVedtak shouldBe true

                // Only one additional request should have been made
                (server?.requestCount ?: 0) - requestCountBefore shouldBe 1
            }
        }
    }
})
