package no.nav.pensjon.simulator.alderspensjon.api.samhandler

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import no.nav.pensjon.simulator.alderspensjon.*
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.forhold.TilknytningService
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@WebMvcTest(SamhandlerAlderspensjonControllerV4::class)
open class SamhandlerAlderspensjonControllerV4Test : ShouldSpec() {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var sporingsloggService: SporingsloggService

    @MockkBean
    private lateinit var feature: FeatureToggleService

    @MockkBean(relaxed = true)
    private lateinit var traceAid: TraceAid

    @MockkBean
    private lateinit var alderspensjonService: AlderspensjonService

    @MockkBean
    private lateinit var organisasjonsnummerProvider: OrganisasjonsnummerProvider

    @MockkBean
    private lateinit var tilknytningService: TilknytningService

    @MockkBean(relaxed = true)
    private lateinit var statistikkService: StatistikkService

    init {
        beforeSpec {
            every { feature.isEnabled(any()) } returns false // vedlikeholdsmodus deaktivert
            every { sporingsloggService.log(any(), any(), any()) } returns Unit
            every { statistikkService.takeSnapshotIfNeeded() } returns Unit
            every { traceAid.callId() } returns "foo"
            every { organisasjonsnummerProvider.provideOrganisasjonsnummer() } returns Organisasjonsnummer("123456789")
            every { tilknytningService.erPersonTilknyttetTjenestepensjonsordning(any(), any()) } returns true
        }

        context("request OK") {
            should("simulere pensjon") {
                every { alderspensjonService.simulerAlderspensjon(any()) } returns AlderspensjonResult(
                    simuleringSuksess = true,
                    aarsakListeIkkeSuksess = emptyList(),
                    alderspensjon = listOf(
                        AlderspensjonFraFolketrygden(
                            fom = LocalDate.of(2035, 1, 1),
                            delytelseListe = listOf(
                                PensjonDelytelse(pensjonType = PensjonType.INNTEKTSPENSJON, beloep = 45000)
                            ),
                            uttaksgrad = Uttaksgrad.FOERTI_PROSENT
                        )
                    ),
                    forslagVedForLavOpptjening = null,
                    harUttak = true,
                    problem = null
                )

                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(requestBody())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk())
                    .andExpect(content().json(OK_RESPONSE_BODY))
            }
        }

        context("bad request - bad value in JSON") {
            should("return status 'bad request' and descriptive body") {
                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(requestBody(grad = "LX")) // invalid value
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(
                        content().json(
                            errorResponseBody(
                                details = "JSON parse error: Unrecognized token 'LX': was expecting" +
                                        " (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"
                            )
                        )
                    )
            }
        }

        context("internal server error") {
            should("return status 'internal server error' and descriptive body") {
                every { alderspensjonService.simulerAlderspensjon(any()) } returns AlderspensjonResult(
                    simuleringSuksess = false,
                    aarsakListeIkkeSuksess = emptyList(),
                    alderspensjon = emptyList(),
                    forslagVedForLavOpptjening = null,
                    harUttak = false,
                    problem = Problem(
                        type = ProblemType.ANNEN_SERVERFEIL,
                        beskrivelse = "intern feil"
                    )
                )

                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(requestBody())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().json(errorResponseBody(details = "intern feil")))
            }
        }
    }
}

private const val URL = "/api/v4/simuler-alderspensjon"

@Language("json")
private fun requestBody(grad: String = "60") = """{
    "personId": "29416429438",
    "gradertUttak": {
        "fraOgMedDato": "2034-07-01",
        "uttaksgrad": $grad
    },
    "heltUttakFraOgMedDato": "2035-01-01",
    "aarIUtlandetEtter16": 10,
    "epsPensjon": true,
    "eps2G": true,
    "fremtidigInntektListe": [
        {
            "aarligInntekt": 123000,
            "fraOgMedDato": "2030-03-01"
        }
    ],
    "rettTilAfpOffentligDato": "2033-06-01"
}""".trimIndent()

@Language("json")
private const val OK_RESPONSE_BODY = """{
    "simuleringSuksess": true,
    "aarsakListeIkkeSuksess": [],
    "alderspensjon": [
        {
            "fraOgMedDato": "2035-01-01",
            "delytelseListe": [
                {
                    "pensjonsType": "inntektsPensjon",
                    "belop": 45000
                }
            ],
            "uttaksgrad": 40
        }
    ],
    "harUttak": true
}"""

@Language("json")
private fun errorResponseBody(details: String) = """{
    "simuleringSuksess": false,
    "aarsakListeIkkeSuksess": [
        {
            "statusKode": "ANNET",
            "statusBeskrivelse": "Call ID: foo | Error: feil ved simulering av alderspensjon V4 | Details: $details"
        }
    ],
    "alderspensjon": [],
    "harUttak": false
}"""