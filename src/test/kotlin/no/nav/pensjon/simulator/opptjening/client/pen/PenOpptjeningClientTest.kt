package no.nav.pensjon.simulator.opptjening.client.pen

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningPersonSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningSpec
import no.nav.pensjon.simulator.opptjening.Pensjonsopptjening
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class PenOpptjeningClientTest : ShouldSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenOpptjeningClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean<WebClientBase>(),
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

    should("innhente opptjeningsgrunnlag og mappe til domenerepresentasjon") {
        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(BODY)
        )

        Arrange.webClientContextRunner().run {
            val result: Pensjonsopptjening = client(context = it).fetchOpptjening(
                OpptjeningMedBeholdningSpec(
                    pid = pid,
                    hentPensjonspoeng = false,
                    hentGrunnlagForOpptjeninger = false,
                    hentBeholdninger = false,
                    harUfoeretrygdKravlinje = false,
                    regelverkType = null,
                    sakType = null,
                    personSpecListe = emptyList(),
                    soekerSpec = OpptjeningMedBeholdningPersonSpec(
                        pid = pid, sisteGyldigeOpptjeningAar = 2024, isGrunnlagRolleSoeker = true
                    )
                )
            )

            with(result) {
                beholdningListe shouldHaveSize 0
                opptjeningGrunnlagListe shouldHaveSize 2
                with(opptjeningGrunnlagListe[0]) {
                    ar shouldBe 2000
                    pi shouldBe 83237
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                inntektGrunnlagListe shouldHaveSize 0
                dagpengerGrunnlagListe shouldHaveSize 0
                omsorgGrunnlagListe shouldHaveSize 0
                forstegangstjeneste shouldBe null
            }
        }
    }
})

@Language("json")
private const val BODY = """{
    "beholdningListe": [],
    "opptjeningGrunnlagListe": [
         {
            "ar": 2000,
            "pi": 83237,
            "pia": 83237,
            "pp": 0.72,
            "opptjeningTypeEnum": "PPI",
            "maksUforegrad": 0,
            "bruk": true,
            "grunnlagKilde": {
                "kode": "POPP",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null
            },
            "opptjeningTypeListe": []
        },
        {
            "ar": 2001,
            "pi": 87067,
            "pia": 87067,
            "pp": 0.72,
            "opptjeningTypeEnum": "PPI",
            "maksUforegrad": 0,
            "bruk": true,
            "grunnlagKildeEnum": "POPP",
            "opptjeningTypeListe": []
        }
    ],
    "inntektGrunnlagListe": [],
    "dagpengerGrunnlagListe": [],
    "omsorgGrunnlagListe": [],
    "forstegangstjeneste": null
}"""