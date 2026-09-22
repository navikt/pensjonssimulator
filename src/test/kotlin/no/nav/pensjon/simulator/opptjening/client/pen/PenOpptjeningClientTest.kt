package no.nav.pensjon.simulator.opptjening.client.pen

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.ForstegangstjenestetypeEnum
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
import java.time.LocalDate

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
                beholdningListe shouldHaveSize 1 // kun Pensjonsbeholdning; AfpOpptjening ignoreres
                with(beholdningListe[0]) {
                    fom shouldBe LocalDate.of(2024, 5, 1)
                    totalbeloep shouldBe 1234.1
                }
                opptjeningsgrunnlagListe shouldHaveSize 2
                with(opptjeningsgrunnlagListe[0]) {
                    ar shouldBe 2000
                    pi shouldBe 83237
                    opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                }
                inntektsgrunnlagListe shouldHaveSize 0
                dagpengegrunnlagListe shouldHaveSize 0
                omsorgsgrunnlagListe shouldHaveSize 0
                with(foerstegangstjeneste?.periodeListe!![0]) {
                    fomDatoLd shouldBe LocalDate.of(1999, 1, 1)
                    periodeTypeEnum shouldBe ForstegangstjenestetypeEnum.TEKN
                }
            }
        }
    }
})

@Language("json")
private const val BODY = """{
  "beholdningListe": [
    {
      "type": "AfpOpptjening",
      "ar": 2025,
      "totalbelop": 123.4
    },
    {
      "type": "Pensjonsbeholdning",
      "ar": 2024,
      "totalbelop": 1234.1,
      "opptjening": {
        "ar": 2022,
        "opptjeningsgrunnlag": 2345.2,
        "anvendtOpptjeningsgrunnlag": 3456.3,
        "arligOpptjening": 4567.4,
        "lonnsvekstInformasjon": null,
        "poengtall": {
          "type": "Poengtall",
          "pp": 1,
          "pia": 2,
          "pi": 3,
          "ar": 2024,
          "bruktIBeregning": true,
          "gv": 4,
          "poengtallTypeEnum": null,
          "maksUforegrad": 5,
          "uforear": false,
          "merknadListe": [],
          "omsorg": false,
          "verdi": 6,
          "opptjeningsar": 2022,
          "inntektIAvtaleland": false,
          "justertBelop": 7
        },
        "inntektUtenDagpenger": 0,
        "uforeOpptjening": {
          "belop": 5678.5,
          "proRataBeregnetUP": false,
          "poengtall": 1.7,
          "ufg": 50,
          "antattInntekt": 6789.6,
          "antattInntekt_proRata": 0,
          "andel_proRata": 0,
          "poengarTeller_proRata": 0,
          "poengarNevner_proRata": 0,
          "antFremtidigeAr_proRata": 0,
          "yrkesskadeopptjening": {
            "paa": 0,
            "yug": 0,
            "antattInntektYrke": 0
          },
          "uforetrygd": false,
          "konvertertUFT": false
        },
        "dagpenger": 0,
        "dagpengerFiskerOgFangstmenn": 0,
        "omsorg": 0,
        "forstegangstjeneste": 0,
        "arligOpptjeningOmsorg": 0,
        "arligOpptjeningUtenOmsorg": 0,
        "psatsOpptjening": 0
      },
      "lonnsvekstInformasjon": {
        "lonnsvekst": 0,
        "reguleringsDatoLd": "2024-05-01",
        "uttaksgradVedRegulering": 0
      },
      "reguleringsInformasjon": {
        "lonnsvekst": 0,
        "fratrekksfaktor": 0,
        "gammelG": 0,
        "nyG": 0,
        "reguleringsfaktor": 0,
        "gjennomsnittligUttaksgradSisteAr": 0,
        "reguleringsbelop": 56401.25902501331,
        "prisOgLonnsvekst": 0
      },
      "formelKodeEnum": null,
      "merknadListe": [],
      "beholdningsTypeEnum": "PEN_B",
      "fomLd": "2024-05-01",
      "tomLd": "2024-12-31"
    }
  ],
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
  "forstegangstjeneste": {
    "periodeListe": [
      {
        "fomDatoLd": "1999-01-01",
        "periodeTypeEnum": "TEKN"
      }
    ]
  }
}"""