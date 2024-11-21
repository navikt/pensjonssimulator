package no.nav.pensjon.simulator.beholdning.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagPersonSpec
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.reactive.function.client.WebClient

class PenBeholdningClientTest : FunSpec({
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

    test("fetchBeholdningerMedGrunnlag") {

        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenBeholdningResponse.BODY)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenBeholdningClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, mock(TraceAid::class.java)
            )

            val result: BeholdningerMedGrunnlagResult = client.fetchBeholdningerMedGrunnlag(
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
                    opptjeningType?.kode shouldBe "PPI"
                }
                inntektGrunnlagListe.size shouldBe 0
                dagpengerGrunnlagListe.size shouldBe 0
                omsorgGrunnlagListe.size shouldBe 0
                foerstegangstjeneste shouldBe null
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
            "opptjeningType": {
                "kode": "PPI",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null
            },
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
            "opptjeningType": {
                "kode": "PPI",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null
            },
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
        }
    ],
    "inntektGrunnlagListe": [],
    "dagpengerGrunnlagListe": [],
    "omsorgGrunnlagListe": [],
    "forstegangstjeneste": null
}"""
}
