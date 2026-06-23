package no.nav.pensjon.simulator.uttak.api

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import no.nav.pensjon.simulator.uttak.TidligstMuligUttak
import no.nav.pensjon.simulator.uttak.UttakService
import no.nav.pensjon.simulator.uttak.Uttaksgrad
import no.nav.pensjon.simulator.uttak.api.acl.UttakSpecMapperV1
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@WebMvcTest(UttakController::class)
class UttakControllerTest : ShouldSpec() {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var sporingsloggService: SporingsloggService

    @MockkBean
    private lateinit var feature: FeatureToggleService

    @MockkBean(relaxed = true)
    private lateinit var traceAid: TraceAid

    @MockkBean
    private lateinit var uttakSpecMapper: UttakSpecMapperV1

    @MockkBean
    private lateinit var uttakService: UttakService

    @MockkBean(relaxed = true)
    private lateinit var statistikkService: StatistikkService

    @MockkBean
    private lateinit var organisasjonsnummerProvider: OrganisasjonsnummerProvider

    @MockkBean
    private lateinit var tilknytningService: TilknytningService

    init {
        beforeSpec {
            every { feature.isEnabled(any()) } returns false // vedlikeholdsmodus deaktivert
            every { sporingsloggService.log(any(), any(), any()) } returns Unit
            every { organisasjonsnummerProvider.provideOrganisasjonsnummer() } returns Organisasjonsnummer("123456789")
            every { uttakSpecMapper.fromSpecV1(any()) } returns TestObjects.simuleringSpec
            every { statistikkService.takeSnapshotIfNeeded() } returns Unit
            every { tilknytningService.erPersonTilknyttetTjenestepensjonsordning(any(), any()) } returns true
            every { traceAid.begin() } returns Unit
        }

        context("request OK") {
            should("simulere tidligst mulig uttak") {
                every { uttakService.finnTidligstMuligUttak(any()) } returns TidligstMuligUttak(
                    uttaksdato = LocalDate.of(2033, 3, 1),
                    uttaksgrad = Uttaksgrad.FEMTI_PROSENT,
                    problem = null
                )

                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(OK_REQUEST_BODY)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk())
                    .andExpect(
                        content().json(
                            """{
  "tidligstMuligeUttakstidspunktListe" : [ {
    "uttaksgrad" : 50,
    "tidligstMuligeUttaksdato" : "2033-03-01"
  } ],
  "feil" : null
}"""
                        )
                    )
            }
        }

        context("bad request - missing parameter in JSON") {
            should("return status 'bad request' and descriptive body") {
                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(BODY_WITH_MISSING_PERSON_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(feilbeskrivelseInneholder("personId"))
            }
        }

        context("bad request - malformed date") {
            should("return status 'bad request' and descriptive body") {
                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(BODY_WITH_BAD_DATE)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(feilbeskrivelseInneholder("2030.02.01"))
            }
        }
    }
}

private const val URL = "/api/v1/tidligst-mulig-uttak"

@Language("json")
private const val OK_REQUEST_BODY = """{
    "personId": "02816396649",
    "fodselsdato": "1963-01-02",
    "uttaksgrad": 50,
    "heltUttakFraOgMedDato": "2030-02-01",
    "rettTilAfpOffentligDato": "2025-02-01",
    "fremtidigInntektListe": [
        {
            "arligInntekt": 637000,
            "fraOgMedDato": "2023-01-01"
        },
        {
            "arligInntekt": 0,
            "fraOgMedDato": "2030-02-01"
        }
    ]
}"""

@Language("json")
private const val BODY_WITH_MISSING_PERSON_ID = """{
    "fodselsdato": "1963-01-02",
    "uttaksgrad": 50,
    "heltUttakFraOgMedDato": "2030-02-01",
    "rettTilAfpOffentligDato": "2025-02-01",
    "fremtidigInntektListe": [
        {
            "arligInntekt": 637000,
            "fraOgMedDato": "2023-01-01"
        },
        {
            "arligInntekt": 0,
            "fraOgMedDato": "2030-02-01"
        }
    ]
}"""

@Language("json")
private const val BODY_WITH_BAD_DATE = """{
    "personId": "02816396649",
    "fodselsdato": "1963-01-02",
    "uttaksgrad": 50,
    "heltUttakFraOgMedDato": "2030.02.01",
    "rettTilAfpOffentligDato": "2025-02-01",
    "fremtidigInntektListe": [
        {
            "arligInntekt": 637000,
            "fraOgMedDato": "2023-01-01"
        },
        {
            "arligInntekt": 0,
            "fraOgMedDato": "2030-02-01"
        }
    ]
}"""

private fun feilbeskrivelseInneholder(value: String): ResultMatcher =
    jsonPath("$.feil.beskrivelse", containsString(value))
