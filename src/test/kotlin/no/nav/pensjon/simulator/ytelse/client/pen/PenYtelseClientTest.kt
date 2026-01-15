package no.nav.pensjon.simulator.ytelse.client.pen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.ProblemType
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanFactory
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate

class PenYtelseClientTest : ShouldSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenYtelseClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            cacheManager = CaffeineCacheManager(),
            traceAid = mockk(relaxed = true),
        )

    beforeSpec {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    should("give løpende ytelser when response is OK") {
        val text: String = this::class.java.getResource("/pen-loepende-ytelser.json")?.readText(Charsets.UTF_8)!!

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(text)
        )

        Arrange.webClientContextRunner().run {
            val result: LoependeYtelserResult = client(context = it).fetchLoependeYtelser(spec = spec())

            with(result) {
                alderspensjon?.sokerVirkningFom shouldBe LocalDate.of(2022, 12, 1)
                with(afpPrivat!!)
                {
                    virkningFom shouldBe LocalDate.of(2024, 5, 1)
                    with(forrigeBeregningsresultat as BeregningsResultatAfpPrivat) {
                        afpPrivatBeregning?.afpPrivatLivsvarig?.afpProsentgrad shouldBe 1.0
                        with(pensjonUnderUtbetaling?.ytelseskomponenter[0] as AfpPrivatLivsvarig) {
                            ytelsekomponentTypeEnum shouldBe YtelseskomponentTypeEnum.AFP_PRIVAT_LIVSVARIG
                            afpForholdstall shouldBe 1.382
                        }
                    }
                }
            }
        }
    }

    context("Personen finnes ikke i vårt system") {
        should("throw 'bad specification' exception") {
            server?.enqueue(
                serverErrorResponse(
                    traceMessage = "ImplementationUnrecoverableException:" +
                            " Personen med fødselsnummer 23456712345 finnes ikke i den lokale oversikten over personer." +
                            " exception.PEN029PersonIkkeFunnetLokaltException\n\tat..."
                )
            )

            Arrange.webClientContextRunner().run {
                shouldThrow<BadSpecException> {
                    client(context = it).fetchLoependeYtelser(spec = spec())
                } shouldBe BadSpecException(
                    message = "Personen finnes ikke i vårt system",
                    problemType = ProblemType.PERSON_IKKE_FUNNET
                )
            }
        }
    }

    context("Personen har ikke løpende alderspensjon") {
        should("throw 'bad specification' exception") {
            server?.enqueue(
                serverErrorResponse(
                    traceMessage = "exception.PEN223BrukerHarIkkeLopendeAlderspensjonException:" +
                            " Bruker har ikke løpende alderspensjon\n\tat..."
                )
            )

            Arrange.webClientContextRunner().run {
                shouldThrow<BadSpecException> {
                    client(context = it).fetchLoependeYtelser(spec = spec())
                } shouldBe BadSpecException(
                    message = "Personen har ikke løpende alderspensjon",
                    problemType = ProblemType.ANNEN_KLIENTFEIL
                )
            }
        }
    }

    context("Personen har løpende alderspensjon på gammelt regelverk") {
        should("throw 'bad specification' exception") {
            server?.enqueue(
                serverErrorResponse(
                    traceMessage = "...PEN226BrukerHarLopendeAPPaGammeltRegelverkException..."
                )
            )

            Arrange.webClientContextRunner().run {
                shouldThrow<BadSpecException> {
                    client(context = it).fetchLoependeYtelser(spec = spec())
                } shouldBe BadSpecException(
                    message = "Personen har løpende alderspensjon på gammelt regelverk",
                    problemType = ProblemType.ANNEN_KLIENTFEIL
                )
            }
        }
    }
})

private fun spec() =
    LoependeYtelserSpec(
        pid,
        foersteUttakDato = LocalDate.MIN,
        avdoed = null,
        alderspensjonFlags = null,
        endringAlderspensjonFlags = null,
        pre2025OffentligAfpYtelserFlags = null
    )

private fun serverErrorResponse(traceMessage: String) =
    MockResponse()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).setBody(
            """{
    "timestamp": "2026-01-15T15:02:48+0100",
    "status": 500,
    "error": "Internal Server Error",
    "trace": $traceMessage,
    "path": "/api/ytelser/v1/loepende"
}"""
        )
