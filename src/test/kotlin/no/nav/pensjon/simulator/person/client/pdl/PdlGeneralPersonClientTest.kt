package no.nav.pensjon.simulator.person.client.pdl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.person.Pid
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
import java.time.LocalDate

class PdlGeneralPersonClientTest : FunSpec({
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

    test("fetchFoedselsdato") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(FOEDSELSDATO_JSON)
        )

        contextRunner.run {
            val client = PdlGeneralPersonClient(
                baseUrl!!,
                webClientBuilder = it.getBean(WebClient.Builder::class.java),
                cacheManager = CaffeineCacheManager(),
                traceAid = mockk(relaxed = true),
                retryAttempts = "0"
            )

            client.fetchFoedselsdato(Pid("22426305678")) shouldBe LocalDate.of(1963, 2, 22)
        }
    }
})

@Language("JSON")
private const val FOEDSELSDATO_JSON =
    """{
              "data": {
                "hentPerson": {
                  "foedselsdato": [
                    {
                      "foedselsdato": "1963-02-22"
                    }
                  ]
                }
              }
            }"""
