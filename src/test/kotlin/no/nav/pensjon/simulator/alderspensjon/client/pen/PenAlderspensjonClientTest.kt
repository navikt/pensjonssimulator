package no.nav.pensjon.simulator.alderspensjon.client.pen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.*
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
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
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class PenAlderspensjonClientTest : FunSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    beforeSpec {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            TestingAuthenticationToken(
                "TEST_USER",
                Jwt("j.w.t", null, null, mapOf("k" to "v"), mapOf("k" to "v"))
            ),
            EgressTokenSuppliersByService(mapOf())
        )

        server = MockWebServer().also { it.start() }
        baseUrl = "http://localhost:${server!!.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    test("simulerAlderspensjon deserializes response") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenResponse.SIMULERT_ALDERSPENSJON)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenAlderspensjonClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, mock(TraceAid::class.java)
            )

            val result = client.simulerAlderspensjon(PenRequest.alderspensjonSpec)

            result shouldBe AlderspensjonResult(
                simuleringSuksess = true,
                aarsakListeIkkeSuksess = emptyList(),
                alderspensjon = listOf(
                    AlderspensjonFraFolketrygden(
                        fom = LocalDate.of(2034, 1, 1),
                        delytelseListe = listOf(
                            PensjonDelytelse(pensjonType = PensjonType.INNTEKTSPENSJON, beloep = 22722),
                            PensjonDelytelse(pensjonType = PensjonType.GARANTIPENSJON, beloep = 17309)
                        ),
                        uttaksgrad = Uttaksgrad.TJUE_PROSENT
                    ),
                    AlderspensjonFraFolketrygden(
                        fom = LocalDate.of(2037, 1, 1),
                        delytelseListe = listOf(
                            PensjonDelytelse(pensjonType = PensjonType.INNTEKTSPENSJON, beloep = 154215),
                            PensjonDelytelse(pensjonType = PensjonType.GARANTIPENSJON, beloep = 74413)
                        ),
                        uttaksgrad = Uttaksgrad.HUNDRE_PROSENT
                    )
                ),
                forslagVedForLavOpptjening = null,
                harUttak = false
            )
        }
    }

    test("simulerAlderspensjon handles utilstrekkelig opptjening") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value()).setBody(PenResponse.UTILSTREKKELIG_OPPTJENING)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenAlderspensjonClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, mock(TraceAid::class.java)
            )

            val result = client.simulerAlderspensjon(PenRequest.alderspensjonSpec)

            result shouldBe
                    AlderspensjonResult(
                        simuleringSuksess = false,
                        aarsakListeIkkeSuksess = listOf(
                            PensjonSimuleringStatus(
                                statusKode = PensjonSimuleringStatusKode.AVSLAG_FOR_LAV_OPPTJENING,
                                statusBeskrivelse = "For lav pensjonsopptjening til å kunne starte uttak med angitt dato og grad"
                            )
                        ),
                        alderspensjon = listOf(
                            AlderspensjonFraFolketrygden(
                                fom = LocalDate.of(2026, 4, 1),
                                delytelseListe = listOf(
                                    PensjonDelytelse(pensjonType = PensjonType.INNTEKTSPENSJON, beloep = 147026),
                                    PensjonDelytelse(pensjonType = PensjonType.GARANTIPENSJON, beloep = 26946)
                                ),
                                uttaksgrad = Uttaksgrad.AATTI_PROSENT
                            ),
                            AlderspensjonFraFolketrygden(
                                fom = LocalDate.of(2027, 2, 1),
                                delytelseListe = listOf(
                                    PensjonDelytelse(pensjonType = PensjonType.INNTEKTSPENSJON, beloep = 185121),
                                    PensjonDelytelse(pensjonType = PensjonType.GARANTIPENSJON, beloep = 33927)
                                ),
                                uttaksgrad = Uttaksgrad.HUNDRE_PROSENT
                            )
                        ),
                        forslagVedForLavOpptjening = ForslagVedForLavOpptjening(
                            gradertUttak = GradertUttak(
                                fom = LocalDate.of(2026, 4, 1),
                                uttaksgrad = Uttaksgrad.AATTI_PROSENT
                            ),
                            heltUttakFom = LocalDate.of(2027, 2, 1)
                        ),
                        harUttak = false
                    )
        }
    }

    test("simulerAlderspensjon handles bad request") {
        val contextRunner = ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientAutoConfiguration::class.java)
        )

        server!!.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.BAD_REQUEST.value()).setBody(PenResponse.ERROR_MESSAGE)
        )

        contextRunner.run {
            val webClientBuilder = it.getBean(WebClient.Builder::class.java)
            val client = PenAlderspensjonClient(
                baseUrl!!, retryAttempts = "0", webClientBuilder, mock(TraceAid::class.java)
            )

            val exception = shouldThrow<EgressException> {
                client.simulerAlderspensjon(PenRequest.alderspensjonSpec)
            }

            exception.message shouldBe """{
    "feilmelding": "OPPTJENING_KontrollerBeregnPensjonsBeholdningRequestRS.BeholdningTomErUgyldig_forSentIAret  (PEN166)",
    "merknader": [
        {
            "ar": null,
            "argumentListeString": "",
            "kode": "OPPTJENING_KontrollerBeregnPensjonsBeholdningRequestRS.BeholdningTomErUgyldig_forSentIAret",
            "argumentListe": []
        }
    ]
}"""
        }
    }
})

object PenRequest {
    val alderspensjonSpec = AlderspensjonSpec(
        pid = Pid("12906498357"),
        gradertUttak = GradertUttakSpec(Uttaksgrad.FEMTI_PROSENT, LocalDate.MIN),
        heltUttakFom = LocalDate.MAX,
        antallAarUtenlandsEtter16Aar = 0,
        epsHarPensjon = false,
        epsHarInntektOver2G = false,
        fremtidigInntektListe = emptyList(),
        rettTilAfpOffentligDato = null
    )
}

object PenResponse {
    @Language("json")
    const val SIMULERT_ALDERSPENSJON = """{
    "simuleringSuksess": true,
    "aarsakListeIkkeSuksess": [],
    "alderspensjon": [
        {
            "fraOgMedDato": "2034-01-01",
            "delytelseListe": [
                {
                    "pensjonsType": "IP",
                    "belop": 22722
                },
                {
                    "pensjonsType": "GAP",
                    "belop": 17309
                }
            ],
            "uttaksgrad": 20
        },
        {
            "fraOgMedDato": "2037-01-01",
            "delytelseListe": [
                {
                    "pensjonsType": "MIN_NIVA_TILL_INDV",
                    "belop": -12402
                },
                {
                    "pensjonsType": "IP",
                    "belop": 154215
                },
                {
                    "pensjonsType": "GAP",
                    "belop": 74413
                }
            ],
            "uttaksgrad": 100
        }
    ],
    "alternativerVedForLavOpptjening": null,
    "harUttak": false
}"""

    @Language("json")
    const val UTILSTREKKELIG_OPPTJENING = """{
    "simuleringSuksess": false,
    "aarsakListeIkkeSuksess": [
        {
            "statusKode": "UTILSTREKKELIG_OPPTJENING",
            "statusBeskrivelse": "For lav pensjonsopptjening til å kunne starte uttak med angitt dato og grad"
        }
    ],
    "alderspensjon": [
        {
            "fraOgMedDato": "2026-04-01",
            "delytelseListe": [
                {
                    "pensjonsType": "IP",
                    "belop": 147026
                },
                {
                    "pensjonsType": "GAP",
                    "belop": 26946
                }
            ],
            "uttaksgrad": 80
        },
        {
            "fraOgMedDato": "2027-02-01",
            "delytelseListe": [
                {
                    "pensjonsType": "MIN_NIVA_TILL_INDV",
                    "belop": -2823
                },
                {
                    "pensjonsType": "IP",
                    "belop": 185121
                },
                {
                    "pensjonsType": "GAP",
                    "belop": 33927
                }
            ],
            "uttaksgrad": 100
        }
    ],
    "alternativerVedForLavOpptjening": {
        "gradertUttak": {
            "fraOgMedDato": "2026-04-01",
            "uttaksgrad": 80
        },
        "heltUttakFraOgMedDato": "2027-02-01"
    },
    "harUttak": false
}"""

    @Language("json")
    const val ERROR_MESSAGE = """{
    "feilmelding": "OPPTJENING_KontrollerBeregnPensjonsBeholdningRequestRS.BeholdningTomErUgyldig_forSentIAret  (PEN166)",
    "merknader": [
        {
            "ar": null,
            "argumentListeString": "",
            "kode": "OPPTJENING_KontrollerBeregnPensjonsBeholdningRequestRS.BeholdningTomErUgyldig_forSentIAret",
            "argumentListe": []
        }
    ]
}"""
}
