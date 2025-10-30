package no.nav.pensjon.simulator.ufoere.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class PenUfoeretrygdUtbetalingClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenUfoeretrygdUtbetalingClient(
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

    test("fetchUtbetalingsgradListe") {
        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenUfoeretrygdUtbetalingResponse.BODY)
        )

        Arrange.webClientContextRunner().run {
            val result: List<UtbetalingsgradUT> = client(context = it).fetchUtbetalingsgradListe(123L)

            result.size shouldBe 3
            with(result[0]) {
                ar shouldBe 2020
                utbetalingsgrad shouldBe 50
            }
            with(result[2]) {
                ar shouldBe 2022
                utbetalingsgrad shouldBe 100
            }
        }
    }
})

object PenUfoeretrygdUtbetalingResponse {

    @Language("json")
    const val BODY = """{
    "utbetalingsgradListe": [
        {
            "ar": 2020,
            "utbetalingsgrad": 50
        },
        {
            "ar": 2021,
            "utbetalingsgrad": 50
        },
        {
            "ar": 2022,
            "utbetalingsgrad": 100
        }
    ]
}"""
}
