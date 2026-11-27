package no.nav.pensjon.simulator.alderspensjon.api.samhandler

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.*
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects
import no.nav.pensjon.simulator.tjenestepensjon.forhold.TilknytningService
import no.nav.pensjon.simulator.validity.BadSpecException
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

@WebMvcTest(SamhandlerAlderspensjonControllerV3::class)
open class SamhandlerAlderspensjonControllerV3Test : ShouldSpec() {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var sporingsloggService: SporingsloggService

    @MockkBean
    private lateinit var feature: FeatureToggleService

    @MockkBean(relaxed = true)
    private lateinit var traceAid: TraceAid

    @MockkBean
    private lateinit var simulatorCore: SimulatorCore

    @MockkBean
    private lateinit var simuleringSpecMapper: AlderspensjonSpecMapperV3

    @MockkBean
    private lateinit var resultMapper: AlderspensjonResultMapperV3

    @MockkBean
    private lateinit var validator: AlderspensjonSpecValidatorV3

    @MockkBean
    private lateinit var statistikkService: StatistikkService

    @MockkBean
    private lateinit var organisasjonsnummerProvider: OrganisasjonsnummerProvider

    @MockkBean
    private lateinit var tilknytningService: TilknytningService

    init {
        beforeSpec {
            every { feature.isEnabled(any()) } returns false // vedlikeholdsmodus deaktivert
            every { sporingsloggService.log(any(), any(), any()) } returns Unit
            every { simuleringSpecMapper.fromDtoV3(any()) } returns TestObjects.simuleringSpec
            every { statistikkService.takeSnapshotIfNeeded() } returns Unit
            every { traceAid.begin(any()) } returns Unit
            every { validator.validate(any()) } returns Unit
            every { organisasjonsnummerProvider.provideOrganisasjonsnummer() } returns Organisasjoner.SPK
            every { tilknytningService.erPersonTilknyttetTjenestepensjonsordning(any(), any()) } returns true
        }

        context("request OK") {
            should("simulere pensjon") {
                every { simulatorCore.simuler(any()) } returns SimulatorOutput()
                every {
                    resultMapper.map(any(), any(), any(), any())
                } returns alderspensjonResult

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

        context("bad enum value in JSON ") {
            should("return status 'bad request' and descriptive body") {
                mvc.perform(
                    post(URL)
                        .content(requestBody(grad = "\"hundre\"")) // ugyldig verdi
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(JSON_ERROR_RESPONSE_BODY))
            }
        }

        context("bad request - personen har ikke løpende alderspensjon") {
            should("return status 'bad request' and descriptive body") {
                every {
                    simulatorCore.simuler(any())
                } throws BadSpecException(
                    message = "Personen har ikke løpende alderspensjon",
                    problemType = ProblemType.ANNEN_KLIENTFEIL
                )

                mvc.perform(
                    post(URL)
                        .content(requestBody())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(SPEC_ERROR_RESPONSE_BODY))
            }
        }
    }
}

private const val URL = "/api/v3/simuler-alderspensjon"

@Language("json")
private fun requestBody(grad: String = "100") = """{
  "fnr": "01416318383",
  "eps2G": true,
  "epsPensjon": true,
  "arIUtlandetEtter16": 1,
  "simulerMedAfpPrivat": false,
  "sivilstandVedPensjonering": "GIFT",
  "forsteUttak": {
    "datoFom": "2029-10-01",
    "grad": $grad
  },
  "fremtidigInntektListe": [
    {
      "fomDato": "2024-10-01",
      "arligInntekt": 7774000
    },
    {
      "fomDato": "2027-10-01",
      "arligInntekt": 1000000
    }
  ]
}""".trimIndent()

private val alderspensjonResult =
    AlderspensjonResultV3(
        pensjonsperioder = listOf(
            PensjonsperiodeResultV3(
                arligUtbetaling = 45504,
                datoFom = "2027-02-01"
            ),
            PensjonsperiodeResultV3(
                arligUtbetaling = 45504,
                datoFom = "2028-02-01"
            ),
            PensjonsperiodeResultV3(
                arligUtbetaling = 122809,
                datoFom = "2029-02-01"
            ),
            PensjonsperiodeResultV3(
                arligUtbetaling = 279200,
                datoFom = "2030-02-01"
            ),
            PensjonsperiodeResultV3(
                arligUtbetaling = 282178,
                datoFom = "2031-02-01"
            ),
            PensjonsperiodeResultV3(
                arligUtbetaling = 48576
            )
        ),
        simuleringsdataListe = listOf(
            SimuleringsdataResultV3(
                forholdstallUttak = 0.0,
                delingstallUttak = 0.0,
                datoFom = "2030-02-01"
            ),
            SimuleringsdataResultV3(
                forholdstallUttak = 0.0,
                delingstallUttak = 15.44,
                datoFom = "2031-01-01"
            )
        ),
        pensjonsbeholdningsperioder = listOf(
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = 3572588.193511122,
                garantipensjonsbeholdning = 940427.1451911018,
                datoFom = "2027-01-01",
                garantipensjonsniva = GarantipensjonsnivaaResultV3(
                    belop = 234765.0,
                    satsType = "ORDINAER",
                    sats = 234765.0,
                    tt_anv = 40
                )
            ),
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = 3745330.050411122,
                garantipensjonsbeholdning = 802233.6596711022,
                datoFom = "2028-01-01",
                garantipensjonsniva = GarantipensjonsnivaaResultV3(
                    belop = 234765.0,
                    satsType = "ORDINAER",
                    sats = 234765.0,
                    tt_anv = 40
                )
            ),
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = 3918071.907311122,
                garantipensjonsbeholdning = 664040.1741511021,
                datoFom = "2029-01-01",
                garantipensjonsniva = GarantipensjonsnivaaResultV3(
                    belop = 234765.0,
                    satsType = "ORDINAER",
                    sats = 234765.0,
                    tt_anv = 40
                )
            ),
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = 3918071.907311122,
                garantipensjonsbeholdning = 664040.1741511021,
                datoFom = "2029-10-01",
                garantipensjonsniva = GarantipensjonsnivaaResultV3(
                    belop = 234765.0,
                    satsType = "ORDINAER",
                    sats = 234765.0,
                    tt_anv = 40
                )
            ),
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = 4090813.764211122,
                garantipensjonsbeholdning = 525846.688631102,
                datoFom = "2030-01-01",
                garantipensjonsniva = GarantipensjonsnivaaResultV3(
                    belop = 234765.0,
                    satsType = "ORDINAER",
                    sats = 234765.0,
                    tt_anv = 40
                )
            ),
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = 4263555.621111122,
                garantipensjonsbeholdning = 387653.2031111019,
                garantitilleggsbeholdning = -2154753.6719627064,
                datoFom = "2031-01-01",
                garantipensjonsniva = GarantipensjonsnivaaResultV3(
                    belop = 234765.0,
                    satsType = "ORDINAER",
                    sats = 234765.0,
                    tt_anv = 40
                )
            )
        ),
        alderspensjonFraFolketrygden = listOf(
            AlderspensjonFraFolketrygdenResultV3(
                datoFom = "2029-10-01",
                delytelser = listOf(
                    DelytelseResultV3(
                        pensjonstype = "MIN_NIVA_TILL_INDV",
                        belop = -42130
                    ),
                    DelytelseResultV3(
                        pensjonstype = "IP",
                        belop = 258569
                    ),
                    DelytelseResultV3(
                        pensjonstype = "GAP",
                        belop = 18326
                    )
                ),
                uttaksgrad = 100
            )
        ),
        harUttak = true,
        harTidligereUttak = false,
        sisteGyldigeOpptjeningsAr = 2024
    )

@Language("json")
private const val OK_RESPONSE_BODY = """{
  "pensjonsperioder": [
    {
      "arligUtbetaling": 45504,
      "datoFom": "2027-02-01"
    },
    {
      "arligUtbetaling": 45504,
      "datoFom": "2028-02-01"
    },
    {
      "arligUtbetaling": 122809,
      "datoFom": "2029-02-01"
    },
    {
      "arligUtbetaling": 279200,
      "datoFom": "2030-02-01"
    },
    {
      "arligUtbetaling": 282178,
      "datoFom": "2031-02-01"
    },
    {
      "arligUtbetaling": 48576
    }
  ],
  "simuleringsdataListe": [
    {
      "forholdstallUttak": 0.0,
      "delingstallUttak": 0.0,
      "datoFom": "2030-02-01"
    },
    {
      "forholdstallUttak": 0.0,
      "delingstallUttak": 15.44,
      "datoFom": "2031-01-01"
    }
  ],
  "pensjonsbeholdningsperioder": [
    {
      "pensjonsbeholdning": 3572588.193511122,
      "garantipensjonsbeholdning": 940427.1451911018,
      "datoFom": "2027-01-01",
      "garantipensjonsniva": {
        "belop": 234765.0,
        "satsType": "ORDINAER",
        "sats": 234765.0,
        "tt_anv": 40
      }
    },
    {
      "pensjonsbeholdning": 3745330.050411122,
      "garantipensjonsbeholdning": 802233.6596711022,
      "datoFom": "2028-01-01",
      "garantipensjonsniva": {
        "belop": 234765.0,
        "satsType": "ORDINAER",
        "sats": 234765.0,
        "tt_anv": 40
      }
    },
    {
      "pensjonsbeholdning": 3918071.907311122,
      "garantipensjonsbeholdning": 664040.1741511021,
      "datoFom": "2029-01-01",
      "garantipensjonsniva": {
        "belop": 234765.0,
        "satsType": "ORDINAER",
        "sats": 234765.0,
        "tt_anv": 40
      }
    },
    {
      "pensjonsbeholdning": 3918071.907311122,
      "garantipensjonsbeholdning": 664040.1741511021,
      "datoFom": "2029-10-01",
      "garantipensjonsniva": {
        "belop": 234765.0,
        "satsType": "ORDINAER",
        "sats": 234765.0,
        "tt_anv": 40
      }
    },
    {
      "pensjonsbeholdning": 4090813.764211122,
      "garantipensjonsbeholdning": 525846.688631102,
      "datoFom": "2030-01-01",
      "garantipensjonsniva": {
        "belop": 234765.0,
        "satsType": "ORDINAER",
        "sats": 234765.0,
        "tt_anv": 40
      }
    },
    {
      "pensjonsbeholdning": 4263555.621111122,
      "garantipensjonsbeholdning": 387653.2031111019,
      "garantitilleggsbeholdning": -2154753.6719627064,
      "datoFom": "2031-01-01",
      "garantipensjonsniva": {
        "belop": 234765.0,
        "satsType": "ORDINAER",
        "sats": 234765.0,
        "tt_anv": 40
      }
    }
  ],
  "alderspensjonFraFolketrygden": [
    {
      "datoFom": "2029-10-01",
      "delytelser": [
        {
          "pensjonstype": "MIN_NIVA_TILL_INDV",
          "belop": -42130
        },
        {
          "pensjonstype": "IP",
          "belop": 258569
        },
        {
          "pensjonstype": "GAP",
          "belop": 18326
        }
      ],
      "uttaksgrad": 100
    }
  ],
  "harUttak": true,
  "harTidligereUttak": false,
  "sisteGyldigeOpptjeningsAr": 2024
}"""

@Language("json")
private const val JSON_ERROR_RESPONSE_BODY = """{
  "feil": "JSON parse error: Cannot deserialize value of type `int` from String \"hundre\": not a valid `int` value",
  "kode": "HttpMessageNotReadableException"
}"""

@Language("json")
private const val SPEC_ERROR_RESPONSE_BODY = """{
  "feil": "Personen har ikke løpende alderspensjon",
  "kode": "BadSpecException"
}"""