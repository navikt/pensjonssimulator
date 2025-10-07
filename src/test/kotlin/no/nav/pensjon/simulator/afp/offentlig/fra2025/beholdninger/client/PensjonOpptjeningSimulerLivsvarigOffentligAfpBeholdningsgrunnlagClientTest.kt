package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.krav.client.pen.PenKravClient
import no.nav.pensjon.simulator.krav.client.pen.PenKravhodeResponse
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.reactive.function.client.WebClient
import java.util.Calendar

class PensjonOpptjeningSimulerLivsvarigOffentligAfpBeholdningsgrunnlagClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            TestingAuthenticationToken(
                "TEST_USER",
                Jwt("j.w.t", null, null, mapOf("k" to "v"), mapOf("k" to "v"))
            ),
            EgressTokenSuppliersByService(mapOf())
        )

        server = MockWebServer().also { it.start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("hent simulerte afp beholdninger") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenKravhodeResponse.KRAVHODE)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenKravClient(
                baseUrl!!,
                retryAttempts = "0",
                webClientBuilder,
                cacheManager = CaffeineCacheManager(),
                traceAid = mockk(relaxed = true)
            )

            val result: Kravhode = client.fetchKravhode(123L)

            result.persongrunnlagListe.size shouldBe 1
            with(result.persongrunnlagListe[0]) {
                personDetaljListe.size shouldBe 1
                personDetaljListe[0].bruk shouldBe true
                fodselsdato shouldBe dateAtNoon(year = 1974, zeroBasedMonth = Calendar.MARCH, day = 30)
            }
            result.kravlinjeListe.size shouldBe 1
            result.kravlinjeListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.UT
        }
    }
})
