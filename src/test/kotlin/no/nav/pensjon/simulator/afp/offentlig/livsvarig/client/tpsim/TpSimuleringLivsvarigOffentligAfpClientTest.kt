package no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.tpsim

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class TpSimuleringLivsvarigOffentligAfpClientTest : FunSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        TpSimuleringLivsvarigOffentligAfpClient(
            baseUrl!!,
            webClientBuilder = context.getBean(WebClient.Builder::class.java),
            retryAttempts = "1",
            traceAid = mockk<TraceAid>(relaxed = true)
        )

    beforeTest {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterTest {
        server?.shutdown()
    }

    test("simuler should return LivsvarigOffentligAfpResult") {
        server?.arrangeOkJsonResponse(okResponseBody)

        Arrange.webClientContextRunner().run {
            client(context = it).simuler(
                spec = LivsvarigOffentligAfpSpec(
                    pid = pid,
                    foedselsdato = LocalDate.of(1963, 1, 1),
                    fom = LocalDate.of(2025, 1, 1),
                    fremtidigInntektListe = emptyList()
                )
            ) shouldBe LivsvarigOffentligAfpResult(
                pid = pid.value,
                afpYtelseListe = listOf(
                    LivsvarigOffentligAfpYtelseMedDelingstall(
                        pensjonBeholdning = 123,
                        afpYtelsePerAar = 23.4,
                        delingstall = 1.2,
                        gjelderFom = LocalDate.of(2024, 6, 1),
                        gjelderFomAlder = Alder(aar = 65, maaneder = 4)
                    )
                )
            )
        }
    }
})

@Language("JSON")
private val okResponseBody: String =
    """{
            "fnr": "12345678910",
            "afpYtelser": [{
               "pensjonsbeholdning" : 123,
               "afpYtelsePerAar" : 23.4,
               "delingstall" : 1.2,
               "gjelderFraOgMed" : "2024-06-01",
               "gjelderFraOgMedAlder" : {
                  "aar": 65,
                  "maaneder": 4
               }
            }]
          }"""
