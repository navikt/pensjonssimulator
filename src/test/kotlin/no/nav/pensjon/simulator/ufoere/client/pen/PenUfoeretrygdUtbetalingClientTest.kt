package no.nav.pensjon.simulator.ufoere.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.reactive.function.client.WebClient

class PenUfoeretrygdUtbetalingClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            TestingAuthenticationToken("TEST_USER", jwt),
            EgressTokenSuppliersByService(mapOf())
        )

        server = MockWebServer().also { it.start() }
        baseUrl = server.let { "http://localhost:${it.port}" }
    }

    afterSpec {
        server?.shutdown()
    }

    test("fetchUtbetalingsgradListe") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenUfoeretrygdUtbetalingResponse.BODY)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenUfoeretrygdUtbetalingClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, CaffeineCacheManager(), mockk(relaxed = true)
            )

            val result: List<UtbetalingsgradUT> = client.fetchUtbetalingsgradListe(123L)

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
