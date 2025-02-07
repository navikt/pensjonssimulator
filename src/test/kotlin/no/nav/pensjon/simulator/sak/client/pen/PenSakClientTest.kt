package no.nav.pensjon.simulator.sak.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
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
import java.util.*

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
                .setResponseCode(HttpStatus.OK.value()).setBody(PenPersonVirkningDatoResponse.RESPONSE_BODY)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenSakClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, CaffeineCacheManager(), mock(TraceAid::class.java)
            )

            val result: FoersteVirkningDatoCombo = client.fetchPersonVirkningDato(pid)

            with(result) {
                foersteVirkningDatoGrunnlagListe.size shouldBe 2

                with(foersteVirkningDatoGrunnlagListe[0]) {
                    virkningsdato shouldBe dateAtNoon(2020, Calendar.FEBRUARY, 1)
                    kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.UT
                }

                with(foersteVirkningDatoGrunnlagListe[1]) {
                    kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
                    kravFremsattDato shouldBe dateAtNoon(2024, Calendar.OCTOBER, 17)
                }
            }
        }
    }
})

object PenPersonVirkningDatoResponse {

    @Language("json")
    const val RESPONSE_BODY = """{
  "forsteVirkningsdatoGrunnlagListe" : [ {
    "virkningsdato" : "2020-02-01",
    "kravFremsattDato" : "2019-11-14",
    "bruker" : {
      "penPersonId" : 30970916,
      "pid" : "22426305678",
      "fodselsdato" : "1963-02-22",
      "afpHistorikkListe" : [ ],
      "uforehistorikk" : {
        "uforeperiodeListe" : [ {
          "ufg" : 50,
          "uft" : "2018-04-04",
          "uforeType" : "UFORE",
          "fppGaranti" : 0.0,
          "redusertAntFppAr" : 0,
          "redusertAntFppAr_proRata" : 0,
          "virk" : "2020-02-01",
          "ufgFom" : "2018-04-04",
          "fodselsArYngsteBarn" : 0,
          "spt" : 0.0,
          "spt_proRata" : 0.0,
          "opt" : 0.0,
          "ypt" : 0.0,
          "spt_pa_f92" : 0,
          "spt_pa_e91" : 0,
          "proRata_teller" : 0,
          "proRata_nevner" : 0,
          "opt_pa_f92" : 0,
          "opt_pa_e91" : 0,
          "ypt_pa_f92" : 0,
          "ypt_pa_e91" : 0,
          "paa" : 0.0,
          "fpp" : 0.0,
          "fpp_omregnet" : 0.0,
          "spt_eos" : 0.0,
          "spt_pa_e91_eos" : 0,
          "spt_pa_f92_eos" : 0,
          "beregningsgrunnlag" : 204097,
          "angittUforetidspunkt" : "2018-04-04",
          "antattInntektFaktorKap19" : 0.7205968498778741,
          "antattInntektFaktorKap20" : 1.720596849877874
        } ],
        "garantigrad" : 0,
        "garantigradYrke" : 0
      },
      "generellHistorikk" : {
        "generellHistorikkId" : 54836715,
        "fpp_eos" : 0.0,
        "giftFor2011" : false
      }
    },
    "kravlinjeType" : "UT"
  }, {
    "virkningsdato" : "2025-03-01",
    "kravFremsattDato" : "2024-10-17",
    "bruker" : {
      "penPersonId" : 30970916,
      "pid" : "22426305678",
      "fodselsdato" : "1963-02-22",
      "afpHistorikkListe" : [ ],
      "uforehistorikk" : {
        "uforeperiodeListe" : [ {
          "ufg" : 50,
          "uft" : "2018-04-04",
          "uforeType" : "UFORE",
          "fppGaranti" : 0.0,
          "redusertAntFppAr" : 0,
          "redusertAntFppAr_proRata" : 0,
          "virk" : "2020-02-01",
          "ufgFom" : "2018-04-04",
          "fodselsArYngsteBarn" : 0,
          "spt" : 0.0,
          "spt_proRata" : 0.0,
          "opt" : 0.0,
          "ypt" : 0.0,
          "spt_pa_f92" : 0,
          "spt_pa_e91" : 0,
          "proRata_teller" : 0,
          "proRata_nevner" : 0,
          "opt_pa_f92" : 0,
          "opt_pa_e91" : 0,
          "ypt_pa_f92" : 0,
          "ypt_pa_e91" : 0,
          "paa" : 0.0,
          "fpp" : 0.0,
          "fpp_omregnet" : 0.0,
          "spt_eos" : 0.0,
          "spt_pa_e91_eos" : 0,
          "spt_pa_f92_eos" : 0,
          "beregningsgrunnlag" : 204097,
          "angittUforetidspunkt" : "2018-04-04",
          "antattInntektFaktorKap19" : 0.7205968498778741,
          "antattInntektFaktorKap20" : 1.720596849877874
        } ],
        "garantigrad" : 0,
        "garantigradYrke" : 0
      },
      "generellHistorikk" : {
        "generellHistorikkId" : 54836715,
        "fpp_eos" : 0.0,
        "giftFor2011" : false
      }
    },
    "kravlinjeType" : "AP"
  } ]
}"""
}
