package no.nav.pensjon.simulator.person.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
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

class PenPersonClientTest : FunSpec({
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

    test("fetchPersonerVedPid") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenPersonHistorikkResponse.BODY)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenPersonClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, CaffeineCacheManager(), mock(TraceAid::class.java)
            )

            val result: Map<Pid, PenPerson> = client.fetchPersonerVedPid(listOf(Pid("22426305678")))

           with(result[Pid("22426305678")]!!) {
               penPersonId shouldBe 123456L
               foedselsdato = LocalDate.of(1963, 2, 22)
               afpHistorikkListe?.size shouldBe 0
               uforehistorikk?.uforeperiodeListe?.size shouldBe 1
               generellHistorikk?.generellHistorikkId shouldBe 54836715
           }
        }
    }
})

object PenPersonHistorikkResponse {

    @Language("json")
    const val BODY = """{
    "personerVedPid": {
        "22426305678": {
            "penPersonId": 123456,
            "pid": "22426305678",
            "fodselsdato": "1963-02-22T12:00:00+0100",
            "afpHistorikkListe": [],
            "uforehistorikk": {
                "uforeperiodeListe": [
                    {
                        "ufg": 50,
                        "uft": "2018-04-04T12:00:00+0200",
                        "uforeType": {
                            "kode": "UFORE",
                            "dekode": null,
                            "dato_fom": null,
                            "dato_tom": null,
                            "er_gyldig": true,
                            "kommentar": null
                        },
                        "fppGaranti": 0.0,
                        "fppGarantiKode": null,
                        "redusertAntFppAr": 0,
                        "redusertAntFppAr_proRata": 0,
                        "proRataBeregningType": null,
                        "virk": "2020-02-01T12:00:00+0100",
                        "uftTom": null,
                        "ufgFom": "2018-04-04T12:00:00+0200",
                        "ufgTom": null,
                        "fodselsArYngsteBarn": 0,
                        "spt": 0.0,
                        "spt_proRata": 0.0,
                        "opt": 0.0,
                        "ypt": 0.0,
                        "spt_pa_f92": 0,
                        "spt_pa_e91": 0,
                        "proRata_teller": 0,
                        "proRata_nevner": 0,
                        "opt_pa_f92": 0,
                        "opt_pa_e91": 0,
                        "ypt_pa_f92": 0,
                        "ypt_pa_e91": 0,
                        "paa": 0.0,
                        "fpp": 0.0,
                        "fpp_omregnet": 0.0,
                        "spt_eos": 0.0,
                        "spt_pa_e91_eos": 0,
                        "spt_pa_f92_eos": 0,
                        "beregningsgrunnlag": 204097,
                        "angittUforetidspunkt": "2018-04-04T12:00:00+0200",
                        "antattInntektFaktorKap19": 0.7205968498778741,
                        "antattInntektFaktorKap20": 1.720596849877874,
                        "realUforeperiode": true,
                        "yp_UFP": null
                    }
                ],
                "garantigrad": 0,
                "garantigradYrke": 0,
                "sistMedlITrygden": null
            },
            "generellHistorikk": {
                "generellHistorikkId": 54836715,
                "fravik_19_3": null,
                "fpp_eos": 0.0,
                "ventetilleggsgrunnlag": null,
                "poengtillegg": null,
                "eosEkstra": null,
                "garantiTrygdetid": null,
                "sertillegg1943kull": null,
                "giftFor2011": false
            }
        }
    }
}"""
}
