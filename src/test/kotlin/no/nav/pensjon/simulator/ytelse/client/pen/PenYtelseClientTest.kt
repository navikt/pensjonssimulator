package no.nav.pensjon.simulator.ytelse.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito.mock
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
import java.time.LocalDate

class PenYtelseClientTest : FunSpec({
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

    test("fetchLoependeYtelser") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        val text: String = this::class.java.getResource("/pen-loepende-ytelser.json")?.readText(Charsets.UTF_8)!!

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(text)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenYtelseClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, CaffeineCacheManager(), mock(TraceAid::class.java)
            )

            val result: LoependeYtelserResult =
                client.fetchLoependeYtelser(LoependeYtelserSpec(pid, LocalDate.MIN, null, null, null, null))

            with(result) {
                alderspensjon?.sokerVirkningFom shouldBe LocalDate.of(2022, 12, 1)
            }
        }
    }
})
