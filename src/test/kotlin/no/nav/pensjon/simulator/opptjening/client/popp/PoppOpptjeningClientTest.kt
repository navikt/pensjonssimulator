package no.nav.pensjon.simulator.opptjening.client.popp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.opptjening.BeholdningSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSpec
import no.nav.pensjon.simulator.opptjening.PensjonspoengSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import no.nav.pensjon.simulator.testutil.arrangeResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate

class PoppOpptjeningClientTest : ShouldSpec({

    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PoppOpptjeningClient(
            baseUrl!!,
            retryAttempts = "1",
            webClientBase = context.getBean<WebClientBase>(),
            cacheManager = mockk(relaxed = true),
            traceAid = mockk(relaxed = true),
            time = { LocalDate.of(2024, 6, 15) } // "dagens dato"
        )

    beforeSpec {
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    context("fetchSistLignedeInntekt") {
        should("returnere inntekt fra POPP") {
            server?.arrangeOkJsonResponse(MINIMAL_OPPTJENINGSGRUNNLAG_RESPONSE_BODY)

            Arrange.security()
            Arrange.webClientContextRunner().run {
                client(context = it).fetchSistLignedeInntekt(Pid("12345678910")) shouldBe LoependeInntekt(
                    aarligBeloep = 123456,
                    fom = LocalDate.of(2025, 1, 1)
                )
            }
        }

        context("tom respons") {
            should("returnere 0 inntekt med f.o.m.-dato lik 1. januar inneværende år") {
                server?.arrangeResponse(HttpStatus.OK, "")

                Arrange.security()
                Arrange.webClientContextRunner().run {
                    client(context = it).fetchSistLignedeInntekt(Pid("12345678910")) shouldBe LoependeInntekt(
                        aarligBeloep = 0,
                        fom = LocalDate.of(2024, 1, 1) // "inneværende år" er 2024 siden "dagens dato" er 2024-06-15
                    )
                }
            }
        }

        context("internal server error") {
            should("forsøke på nytt") {
                server?.arrangeResponse(HttpStatus.INTERNAL_SERVER_ERROR, "feil") // respons ved 1. forsøk
                server?.arrangeOkJsonResponse(MINIMAL_OPPTJENINGSGRUNNLAG_RESPONSE_BODY) // respons ved 2. forsøk

                Arrange.security()
                Arrange.webClientContextRunner().run {
                    client(context = it).fetchSistLignedeInntekt(Pid("12345678910")).aarligBeloep shouldBe 123456
                }
            }
        }
    }

    context("fetchOpptjeningsgrunnlag") {
        should("innhente opptjeningsgrunnlag og mappe til domenerepresentasjon") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value()).setBody(loadJson(tema = "opptjeningsgrunnlag"))
            )

            Arrange.security()
            Arrange.webClientContextRunner().run {
                val result = client(context = it).fetchOpptjeningsgrunnlag(
                    OpptjeningsgrunnlagSpec(
                        pid = pid,
                        grunnlagstypeListe = emptyList()
                    )
                )
                with(result) {
                    inntektListe shouldHaveSize 6
                    with(inntektListe[0]) {
                        inntektTypeEnum shouldBe null
                        fomLd shouldBe LocalDate.of(2015, 1, 1)
                        tomLd shouldBe LocalDate.of(2015, 12, 31)
                        belop shouldBe 555000
                        grunnlagKildeEnum shouldBe GrunnlagkildeEnum.PEN
                        bruk shouldBe true
                        erRelevant shouldBe false
                    }
                    with(inntektListe[1]) {
                        inntektTypeEnum shouldBe null
                        fomLd shouldBe LocalDate.of(2015, 1, 1)
                        tomLd shouldBe LocalDate.of(2015, 12, 31)
                        belop shouldBe 555001
                        grunnlagKildeEnum shouldBe GrunnlagkildeEnum.POPP
                        bruk shouldBe true
                        erRelevant shouldBe false
                    }
                    with(inntektListe[2]) {
                        inntektTypeEnum shouldBe InntekttypeEnum.AI
                        fomLd shouldBe LocalDate.of(2019, 1, 1)
                        tomLd shouldBe LocalDate.of(2019, 12, 31)
                        belop shouldBe 555002
                        grunnlagKildeEnum shouldBe GrunnlagkildeEnum.PEN
                        bruk shouldBe true
                        erRelevant shouldBe false
                    }
                    with(inntektListe[3]) {
                        inntektTypeEnum shouldBe InntekttypeEnum.ARBLIGN
                        fomLd shouldBe LocalDate.of(2020, 1, 1)
                        tomLd shouldBe LocalDate.of(2020, 12, 31)
                        belop shouldBe 555003
                        grunnlagKildeEnum shouldBe GrunnlagkildeEnum.POPP
                        bruk shouldBe true
                        erRelevant shouldBe false
                    }
                    with(inntektListe[4]) {
                        grunnlagKildeEnum shouldBe GrunnlagkildeEnum.OVRIG // Ingen 'SKD' i GrunnlagkildeEnum
                    }
                    with(inntektListe[5]) {
                        grunnlagKildeEnum shouldBe GrunnlagkildeEnum.OVRIG // Ingen 'SKD' i GrunnlagkildeEnum
                    }
                }
            }
        }
    }

    context("fetchBeholdninger") {
        should("innhente beholdninger og mappe til domenerepresentasjon") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value()).setBody(loadJson(tema = "beholdning"))
            )

            Arrange.security()
            Arrange.webClientContextRunner().run {
                val result: List<Pensjonsbeholdning> = client(context = it).fetchBeholdninger(
                    BeholdningSpec(
                        pid,
                        beholdningType = BeholdningtypeEnum.PEN_B,
                        serviceDirektiv = "INKL_OPPTJENINGBELOP"
                    )
                )
                with(result) {
                    this shouldHaveSize 4
                    with(this[0]) {
                        ar shouldBe 1989
                        fomLd shouldBe LocalDate.of(1989, 1, 1)
                        tomLd shouldBe LocalDate.of(1989, 12, 31)
                        totalbelop shouldBe 36547.9171028804
                        with(opptjening!!) {
                            ar shouldBe 1987
                            opptjeningsgrunnlag shouldBe 191561.2
                            anvendtOpptjeningsgrunnlag shouldBe 191500.0
                            arligOpptjening shouldBe 36547.9171028804
                            arligOpptjeningUtenOmsorg shouldBe 34672.541
                            forstegangstjeneste shouldBe 0.0
                            inntektUtenDagpenger shouldBe 191561.3
                            omsorg shouldBe 0.0
                            uforeOpptjening shouldBe null
                            dagpenger shouldBe 0.0
                            dagpengerFiskerOgFangstmenn shouldBe 0.0
                        }
                        lonnsvekstInformasjon shouldBe null
                        reguleringsInformasjon shouldBe null
                    }
                    with(this[3]) {
                        ar shouldBe 1992
                        fomLd shouldBe LocalDate.of(1992, 1, 1)
                        tomLd shouldBe LocalDate.of(1992, 12, 31)
                        totalbelop shouldBe 166014.511396495
                        with(opptjening!!) {
                            dagpenger shouldBe 213278.846153846
                            dagpengerFiskerOgFangstmenn shouldBe 1234.5
                        }
                        with(lonnsvekstInformasjon!!) {
                            lonnsvekst shouldBe 0.0
                            reguleringsDatoLd shouldBe LocalDate.of(1991, 12, 31)
                            uttaksgradVedRegulering shouldBe 0
                        }
                        with(reguleringsInformasjon!!) {
                            lonnsvekst shouldBe 0.0
                            fratrekksfaktor shouldBe 0.0
                            gammelG shouldBe 0
                            nyG shouldBe 0
                            reguleringsfaktor shouldBe 0.0
                            gjennomsnittligUttaksgradSisteAr shouldBe 0.0
                            reguleringsbelop shouldBe 5181.87992977412
                            prisOgLonnsvekst shouldBe 0.0
                        }
                    }
                }
            }
        }
    }

    context("fetchPensjonspoeng") {
        should("innhente pensjonspoeng og mappe til domenerepresentasjon") {
            server?.enqueue(
                MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(HttpStatus.OK.value()).setBody(loadJson(tema = "pensjonspoeng"))
            )

            Arrange.security()
            Arrange.webClientContextRunner().run {
                val result: List<Opptjeningsgrunnlag> = client(context = it).fetchPensjonspoeng(
                    PensjonspoengSpec(pid)
                )
                with(result) {
                    this shouldHaveSize 5
                    with(this[0]) {
                        ar shouldBe 2001
                        pp shouldBe 1.45
                        opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
                        pi shouldBe 124205
                        pia shouldBe 124200
                        maksUforegrad shouldBe 0
                    }
                }
            }
        }
    }
})

@Language("JSON")
private const val MINIMAL_OPPTJENINGSGRUNNLAG_RESPONSE_BODY: String =
    """{
              "opptjeningsGrunnlag": {
                "inntektListe": [{
                  "inntektType": "SUM_PI",
                  "inntektAr": 2025,
                  "belop": 123456
                }]
              }
            }"""


private fun ShouldSpec.loadJson(tema: String): String =
    this::class.java.getResource("/response/popp-$tema.json")?.readText(Charsets.UTF_8)!!