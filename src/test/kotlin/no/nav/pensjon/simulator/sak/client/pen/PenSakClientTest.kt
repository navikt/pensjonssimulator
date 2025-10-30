package no.nav.pensjon.simulator.sak.client.pen

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.*

class PenSakClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null
    val defaultTimeZone = TimeZone.getDefault()

    fun client(context: BeanFactory) =
        PenSakClient(
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
        TimeZone.setDefault(TimeZone.getTimeZone("CET"))
    }

    afterSpec {
        server?.shutdown()
        TimeZone.setDefault(defaultTimeZone)
    }

    test("fetchPersonVirkningDato") {
        server?.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenPersonVirkningDatoResponse.RESPONSE_BODY)
        )

        Arrange.webClientContextRunner().run {
            val result: FoersteVirkningDatoCombo = client(context = it).fetchPersonVirkningDato(pid)

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
