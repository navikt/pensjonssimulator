package no.nav.pensjon.simulator.sak.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
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
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.util.TimeZone

class PenSakClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null
    val defaultTimeZone = TimeZone.getDefault()

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            TestingAuthenticationToken("TEST_USER", jwt),
            EgressTokenSuppliersByService(mapOf())
        )

        server = MockWebServer().also { it.start() }
        baseUrl = server.let { "http://localhost:${it.port}" }
        TimeZone.setDefault(TimeZone.getTimeZone("CET"))
    }

    afterSpec {
        server?.shutdown()
        TimeZone.setDefault(defaultTimeZone)
    }

    test("fetchPersonVirkningDato") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenPersonVirkningDatoResponse.BODY)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenSakClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, CaffeineCacheManager(), mock(TraceAid::class.java)
            )

            val result: FoersteVirkningDatoCombo = client.fetchPersonVirkningDato(pid)

            with(result) {
                person.penPersonId shouldBe 123456L
                foersteVirkningDatoListe.size shouldBe 2
                foersteVirkningDatoListe[0].virkningDato shouldBe LocalDate.of(2020, 2, 1)
                foersteVirkningDatoGrunnlagListe.size shouldBe 2
            }
        }
    }
})

object PenPersonVirkningDatoResponse {

    @Language("json")
    const val BODY = """{
    "person": {
        "penPersonId": 123456
    },
    "forsteVirkningsdatoListe": [
        {
            "sakType": "UFOREP",
            "kravlinjeType": "UT",
            "virkningsdato": "2020-02-01T12:00:00+0100",
            "annenPerson": null
        },
        {
            "sakType": "ALDER",
            "kravlinjeType": "AP",
            "virkningsdato": "2025-03-01T12:00:00+0100",
            "annenPerson": null
        }
    ],
    "forsteVirkningsdatoGrunnlagListe": [
        {
            "virkningsdato": "2020-02-01T00:00:00+0100",
            "kravFremsattDato": "2019-11-14T12:00:00+0100",
            "bruker": {
                "penPersonId": 123456
            },
            "annenPerson": null,
            "kravlinjeType": {
                "kode": "UT",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null,
                "hovedKravlinje": true
            }
        },
        {
            "virkningsdato": "2025-03-01T12:00:00+0100",
            "kravFremsattDato": "2024-10-17T12:00:00+0200",
            "bruker": {
                "penPersonId": 123456
            },
            "annenPerson": null,
            "kravlinjeType": {
                "kode": "AP",
                "dekode": null,
                "dato_fom": null,
                "dato_tom": null,
                "er_gyldig": true,
                "kommentar": null,
                "hovedKravlinje": true
            }
        }
    ]
}"""
}
