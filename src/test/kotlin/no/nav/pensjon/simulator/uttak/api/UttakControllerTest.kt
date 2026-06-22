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
                        .content(okRequestBody())
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
                        .content(bodyWithMissingPersonId())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(
                        content().json(
                            """{
  "tidligstMuligeUttakstidspunktListe" : [],
  "feil" : {
    "type" : "ANNEN_KLIENTFEIL",
    "beskrivelse" : "Parameter specified as non-null is null: method no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakSpecV1.<init>, parameter personId"
  }
}"""
                        )
                    )
            }
        }

        context("bad request - malformed date") {
            should("return status 'bad request' and descriptive body") {
                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(bodyWithBadDate())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(
                        content().json(
                            """{
  "tidligstMuligeUttakstidspunktListe": [],
  "feil": {
    "type": "ANNEN_KLIENTFEIL",
    "beskrivelse": "Cannot deserialize value of type `java.time.LocalDate` from String \"2030.02.01\": Failed to deserialize `java.time.LocalDate` (with format 'Value(YearOfEra,4,19,EXCEEDS_PAD)'-'Value(MonthOfYear,2)'-'Value(DayOfMonth,2)'): (java.time.format.DateTimeParseException) Text '2030.02.01' could not be parsed at index 4\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); byte offset: #117] (through reference chain: no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakSpecV1[\"heltUttakFraOgMedDato\"])"
  }
}"""
                        )
                    )
            }
        }
    }

    private companion object {

        private const val URL = "/api/v1/tidligst-mulig-uttak"

        @Language("json")
        private fun okRequestBody() = """{
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
}""".trimIndent()

        @Language("json")
        private fun bodyWithMissingPersonId() = """{
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
}""".trimIndent()

        @Language("json")
        private fun bodyWithBadDate() = """{
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
}""".trimIndent()
    }
}