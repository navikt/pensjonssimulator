package no.nav.pensjon.simulator.ytelse.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
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

class PenYtelseClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenYtelseClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            cacheManager = CaffeineCacheManager(),
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

    test("fetchLoependeYtelser") {
        val text: String = this::class.java.getResource("/pen-loepende-ytelser.json")?.readText(Charsets.UTF_8)!!

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(text)
        )

        Arrange.webClientContextRunner().run {
            val result: LoependeYtelserResult =
                client(context = it).fetchLoependeYtelser(
                    spec = LoependeYtelserSpec(
                        pid = pid,
                        foersteUttakDato = LocalDate.MIN,
                        avdoed = null,
                        alderspensjonFlags = null,
                        endringAlderspensjonFlags = null,
                        pre2025OffentligAfpYtelserFlags = null
                    )
                )

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
})
