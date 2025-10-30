package no.nav.pensjon.simulator.beholdning.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagPersonSpec
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class PenBeholdningClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenBeholdningClient(
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

    test("fetchBeholdningerMedGrunnlag") {
        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenBeholdningResponse.BODY)
        )

        Arrange.webClientContextRunner().run {
            val result: BeholdningerMedGrunnlagResult = client(context = it).fetchBeholdningerMedGrunnlag(
                BeholdningerMedGrunnlagSpec(
                    pid = pid,
                    hentPensjonspoeng = false,
                    hentGrunnlagForOpptjeninger = false,
                    hentBeholdninger = false,
                    harUfoeretrygdKravlinje = false,
                    regelverkType = null,
                    sakType = null,
                    personSpecListe = emptyList(),
                    soekerSpec = BeholdningerMedGrunnlagPersonSpec(
                        pid = pid, sisteGyldigeOpptjeningAar = 2024, isGrunnlagRolleSoeker = true
                    )
                )
            )

            with(result) {
                beholdningListe.size shouldBe 0
                opptjeningGrunnlagListe.size shouldBe 2
                with(opptjeningGrunnlagListe[0]) {
                    ar shouldBe 2000
                    pi shouldBe 83237
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                inntektGrunnlagListe.size shouldBe 0
                dagpengerGrunnlagListe.size shouldBe 0
                omsorgGrunnlagListe.size shouldBe 0
                forstegangstjeneste shouldBe null
            }
        }
    }
})

object PenBeholdningResponse {

    @Language("json")
    const val BODY = """{
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
}
