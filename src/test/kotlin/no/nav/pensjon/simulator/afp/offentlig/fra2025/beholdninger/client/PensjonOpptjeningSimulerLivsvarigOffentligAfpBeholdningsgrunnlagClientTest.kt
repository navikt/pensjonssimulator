package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

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
                .setResponseCode(HttpStatus.OK.value()).setBody(AfpBeholdingAPIResponse.BEHOLDNINGER)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PensjonOpptjeningSimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient(
                baseUrl!!,
                retryAttempts = "0",
                traceAid = mockk(relaxed = true),
                webClientBuilder,
            )

            val result: List<SimulerLivsvarigOffentligAfpBeholdningsperiode> = client.simulerAfpBeholdningGrunnlag(
                LivsvarigOffentligAfpSpec(
                    pid = Pid("12345678901"),
                    foedselsdato = LocalDate.now(),
                    fom = LocalDate.parse("2035-02-01"),
                    fremtidigInntektListe = emptyList(),
                )
            )

            result.size shouldBe 2
            with(result[0]) {
                pensjonsbeholdning shouldBe 123456
                fom shouldBe LocalDate.parse("2035-02-01")
            }
            with(result[1]) {
                pensjonsbeholdning shouldBe 199999
                fom shouldBe LocalDate.parse("2036-01-01")
            }
        }
    }

    test("haandterer tom response fra afp-opptjening-api ") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody("{\"afpBeholdningsgrunnlag\": [] }")
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PensjonOpptjeningSimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient(
                baseUrl!!,
                retryAttempts = "0",
                traceAid = mockk(relaxed = true),
                webClientBuilder,
            )

            val result: List<SimulerLivsvarigOffentligAfpBeholdningsperiode> = client.simulerAfpBeholdningGrunnlag(
                LivsvarigOffentligAfpSpec(
                    pid = Pid("12345678901"),
                    foedselsdato = LocalDate.now(),
                    fom = LocalDate.parse("2035-02-01"),
                    fremtidigInntektListe = emptyList(),
                )
            )

            result.size shouldBe 0
        }
    }

})

object AfpBeholdingAPIResponse {

    @Language("json")
    const val BEHOLDNINGER = """
{
  "afpBeholdningsgrunnlag": [
    {
      "fraOgMedDato": "2035-02-01",
      "belop": 123456
    },
    {
      "fraOgMedDato": "2036-01-01",
      "belop": 199999
    }
  ]
}
    """
}