package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate

class PensjonOpptjeningSimulerLivsvarigOffentligAfpBeholdningsgrunnlagClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PensjonOpptjeningSimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            traceAid = mockk(relaxed = true),
        )

    beforeSpec {
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("hent simulerte AFP-beholdninger") {
        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(AfpBeholdningApiResponse.BEHOLDNINGER)
        )

        Arrange.security()
        Arrange.webClientContextRunner().run {
            val result: List<SimulerLivsvarigOffentligAfpBeholdningsperiode> =
                client(context = it).simulerAfpBeholdningGrunnlag(
                    LivsvarigOffentligAfpSpec(
                        pid = Pid("12345678901"),
                        foedselsdato = LocalDate.now(),
                        fom = LocalDate.of(2035, 2, 1),
                        fremtidigInntektListe = emptyList(),
                    )
                )

            result shouldHaveSize 2
            with(result[0]) {
                pensjonsbeholdning shouldBe 123456
                fom shouldBe LocalDate.of(2035, 2, 1)
            }
            with(result[1]) {
                pensjonsbeholdning shouldBe 199999
                fom shouldBe LocalDate.of(2036, 1, 1)
            }
        }
    }

    test("håndterer tom response fra afp-opptjening-api") {
        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody("{\"afpBeholdningsgrunnlag\": [] }")
        )

        Arrange.security()
        Arrange.webClientContextRunner().run {
            val result: List<SimulerLivsvarigOffentligAfpBeholdningsperiode> =
                client(context = it).simulerAfpBeholdningGrunnlag(
                    LivsvarigOffentligAfpSpec(
                        pid = Pid("12345678901"),
                        foedselsdato = LocalDate.now(),
                        fom = LocalDate.of(2035, 2, 1),
                        fremtidigInntektListe = emptyList(),
                    )
                )

            result shouldHaveSize 0
        }
    }
})

private object AfpBeholdningApiResponse {

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
