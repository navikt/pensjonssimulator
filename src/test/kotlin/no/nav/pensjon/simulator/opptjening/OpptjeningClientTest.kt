package no.nav.pensjon.simulator.opptjening

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import no.nav.pensjon.simulator.testutil.arrangeResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.http.HttpStatus
import java.time.LocalDate

class OpptjeningClientTest : FunSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        OpptjeningClient(
            baseUrl!!,
            retryAttempts = "1",
            webClientBase = context.getBean(WebClientBase::class.java),
            traceAid = mockk<TraceAid>(relaxed = true),
            time = { LocalDate.of(2024, 6, 15) } // "dagens dato"
        )

    beforeSpec {
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("skal returnere inntekt fra POPP") {
        server?.arrangeOkJsonResponse(okResponseBody)

        Arrange.security()
        Arrange.webClientContextRunner().run {
            client(context = it).hentSisteLignetInntekt(Pid("12345678910")) shouldBe Inntekt(
                aarligBeloep = 123456,
                fom = LocalDate.of(2025, 1, 1)
            )
        }
    }

    test("hvis tom respons: skal returnere 0 inntekt med f.o.m.-dato lik 1. januar inneværende år") {
        server?.arrangeResponse(HttpStatus.OK, "")

        Arrange.security()
        Arrange.webClientContextRunner().run {
            client(context = it).hentSisteLignetInntekt(Pid("12345678910")) shouldBe Inntekt(
                aarligBeloep = 0,
                fom = LocalDate.of(2024, 1, 1) // "inneværende år" er 2024 siden "dagens dato" er 2024-06-15
            )
        }
    }

    test("skal forsøke på nytt ved 'internal server error'") {
        server?.arrangeResponse(HttpStatus.INTERNAL_SERVER_ERROR, "feil") // respons ved 1. forsøk
        server?.arrangeOkJsonResponse(okResponseBody) // respons ved 2. forsøk

        Arrange.security()
        Arrange.webClientContextRunner().run {
            client(context = it).hentSisteLignetInntekt(Pid("12345678910")).aarligBeloep shouldBe 123456
        }
    }
})

@Language("JSON")
private val okResponseBody: String =
    """{
              "opptjeningsGrunnlag": {
                "inntektListe": [{
                  "inntektType": "SUM_PI",
                  "inntektAr": 2025,
                  "belop": 123456
                }]
              }
            }"""
