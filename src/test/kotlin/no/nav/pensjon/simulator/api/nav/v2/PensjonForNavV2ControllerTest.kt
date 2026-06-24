package no.nav.pensjon.simulator.api.nav.v2

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.api.nav.v2.acl.spec.SimuleringSpecMapperForNavV2
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulertOpptjening
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.testutil.TestObjects
import no.nav.pensjon.simulator.testutil.TestObjects.emptyKnekkpunkter
import no.nav.pensjon.simulator.trygdetid.Trygdetid
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

@WebMvcTest(PensjonForNavV2Controller::class)
open class PensjonForNavV2ControllerTest : ShouldSpec() {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var sporingsloggService: SporingsloggService

    @MockkBean
    private lateinit var feature: FeatureToggleService

    @MockkBean(relaxed = true)
    private lateinit var traceAid: TraceAid

    @MockkBean
    private lateinit var simuleringSpecMapper: SimuleringSpecMapperForNavV2

    @MockkBean
    private lateinit var simuleringFacade: SimuleringFacade

    @MockkBean(relaxed = true)
    private lateinit var statistikkService: StatistikkService

    init {
        beforeSpec {
            every { feature.isEnabled(any()) } returns false // vedlikeholdsmodus deaktivert
            every { sporingsloggService.log(any(), any(), any()) } returns Unit
            every { simuleringSpecMapper.fromDto(any()) } returns TestObjects.simuleringSpec
            every { statistikkService.takeSnapshotIfNeeded() } returns Unit
            every { traceAid.begin() } returns Unit
        }

        context("request OK") {
            should("simulere pensjon") {
                every {
                    simuleringFacade.simulerAlderspensjon(any(), any())
                } returns pensjonMedAlternativ

                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(requestBody())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk())
                    .andExpect(content().json(OK_RESPONSE_BODY))

                verify { sporingsloggService wasNot Called } // not called in Nav context
            }
        }

        context("bad request - bad enum value in JSON") {
            should("return status 'bad request' and descriptive body") {
                mvc.perform(
                    post(URL)
                        .with(csrf())
                        .content(requestBody(grad = "TI_PROSENT")) // invalid value
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(ERROR_RESPONSE_BODY))
            }
        }
    }

    private companion object {

        private const val URL = "/api/nav/v2/simuler-pensjon"

        @Language("json")
        private fun requestBody(grad: String = "FEMTI_PROSENT") = """{
    "simuleringstype": "ALDERSPENSJON_MED_TIDSBEGRENSET_OFFENTLIG_AFP",
    "pid": "29416429438",
    "sivilstatus": "UGIFT",
    "sisteInntekt": 763215,
    "gradertUttak": {
        "grad": "$grad",
        "uttakFomAlder": {
            "aar": 65,
            "maaneder": 3
        },
        "aarligInntekt": 333000
    },
    "heltUttak": {
        "uttakFomAlder": {
            "aar": 66,
            "maaneder": 3
        },
        "aarligInntekt": 0,
        "inntektTomAlder": {
            "aar": 69,
            "maaneder": 6
        }
    },
    "utenlandsperiodeListe": [],
    "eps": {
        "levende": {
            "harPensjon": false,
            "harInntektOver2G": false
        }
    },
    "offentligAfp": {
        "harInntektMaanedenFoerUttak": false,
        "afpOrdning": "KOMMUNAL",
        "innvilgetLivsvarigAfp": null
    }
}""".trimIndent()

        @Language("json")
        private const val OK_RESPONSE_BODY = """{
  "alderspensjonListe": [{
    "alderAar": 65,
    "beloep": 123,
    "inntektspensjon": 234,
    "delingstall": 1.2,
    "pensjonsbeholdningFoerUttak": 456,
    "sluttpoengtall": 5.6,
    "poengaarFoer92": 10,
    "poengaarEtter91": 20,
    "forholdstall": 0.8,
    "grunnpensjon": 567,
    "tilleggspensjon": 678,
    "pensjonstillegg": 789,
    "skjermingstillegg": 890,
    "kapittel19Pensjon": {
      "andelsbroek": 0.3,
      "trygdetidAntallAar": 30,
      "basispensjon": 321,
      "restpensjon": 876,
      "gjenlevendetillegg": 321,
      "minstePensjonsnivaaSats": 0.9
    },
    "kapittel20Pensjon": {
      "andelsbroek": 0.7,
      "trygdetidAntallAar": 40,
      "garantipensjon": {
        "aarligBeloep": 345,
        "maanedligBeloep": null,
        "sats": 1.1
      },
      "garantitillegg": 432
    }
  }],
  "alderspensjonMaanedsbeloep": {
    "gradertUttakBeloep": 678,
    "heltUttakBeloep": 0
  },
  "maanedligAlderspensjonForKnekkpunkter": {},
  "livsvarigOffentligAfpListe": [{
    "alderAar": 63,
    "beloep": 901,
    "maanedligBeloep": 100
  }],
  "tidsbegrensetOffentligAfp": {
    "alderAar": 62,
    "totaltAfpBeloep": 890,
    "tidligereArbeidsinntekt": 123,
    "grunnbeloep": 456,
    "sluttpoengtall": 7.8,
    "trygdetid": 30,
    "poengaarTom1991": 10,
    "poengaarFom1992": 20,
    "grunnpensjon": 567,
    "tilleggspensjon": 678,
    "afpTillegg": 789,
    "saertillegg": 890,
    "afpGrad": 80,
    "erAvkortet": false
  },
  "privatAfpListe": [{
    "alderAar": 66,
    "beloep": 789,
    "kompensasjonstillegg": 123,
    "kronetillegg": 5,
    "livsvarig": 93,
    "maanedligBeloep": 100
  }],
  "primaerTrygdetid": {
    "antallAar": 39,
    "erUtilstrekkelig": false
  },
  "vilkaarsproevingsresultat": {
    "erInnvilget": false,
    "alternativ": {
      "gradertUttakAlder": {
        "aar": 66,
        "maaneder": 2
      },
      "uttaksgrad": "TJUE_PROSENT",
      "heltUttakAlder": {
        "aar": 67,
        "maaneder": 0
      }
    }
  },
  "pensjonsgivendeInntektListe": [{
    "aarstall": 1999,
    "beloep": 1002
  }]
}"""

        @Language("json")
        private const val ERROR_RESPONSE_BODY = """{
  "alderspensjonListe": [],
  "livsvarigOffentligAfpListe": [],
  "privatAfpListe": [],
  "vilkaarsproevingsresultat": {
    "erInnvilget": false
  },
  "pensjonsgivendeInntektListe": [],
  "problem": {
    "kode": "ANNEN_KLIENTFEIL",
    "beskrivelse": "Cannot deserialize value of type `no.nav.pensjon.simulator.api.nav.v2.acl.UttaksgradDto` from String \"TI_PROSENT\": not one of the values accepted for Enum class: [FOERTI_PROSENT, SEKSTI_PROSENT, NULL, FEMTI_PROSENT, HUNDRE_PROSENT, AATTI_PROSENT, TJUE_PROSENT]\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); byte offset: #194] (through reference chain: no.nav.pensjon.simulator.api.nav.v2.acl.spec.SimuleringSpecDto[\"gradertUttak\"]->no.nav.pensjon.simulator.api.nav.v2.acl.spec.GradertUttakSpecDto[\"grad\"])"
  }
}"""

        private val pensjonMedAlternativ =
            SimulertPensjonEllerAlternativ(
                pensjon = SimulertPensjon(
                    alderspensjon = listOf(
                        SimulertAarligAlderspensjon(
                            alderAar = 65,
                            beloep = 123,
                            inntektspensjon = 234,
                            garantipensjon = SimulertGarantipensjon(aarligBeloep = 345, sats = 1.1),
                            garantitillegg = 432,
                            delingstall = 1.2,
                            pensjonBeholdningFoerUttak = 456,
                            andelsbroekKap19 = 0.3,
                            andelsbroekKap20 = 0.7,
                            sluttpoengtall = 5.6,
                            trygdetidKap19 = 30,
                            trygdetidKap20 = 40,
                            poengaarFoer92 = 10,
                            poengaarEtter91 = 20,
                            forholdstall = 0.8,
                            basispensjon = 321,
                            grunnpensjon = 567,
                            tilleggspensjon = 678,
                            restpensjon = 876,
                            pensjonstillegg = 789,
                            skjermingstillegg = 890,
                            kapittel19Gjenlevendetillegg = 321,
                            minstePensjonsnivaaSats = 0.9
                        )
                    ),
                    maanedligAlderspensjonForKnekkpunkter = emptyKnekkpunkter,
                    alderspensjonFraFolketrygden = listOf(
                        SimulertAlderspensjonFraFolketrygden(
                            datoFom = LocalDate.of(2021, 1, 1),
                            delytelseListe = listOf(
                                SimulertDelytelse(type = YtelseskomponentTypeEnum.GAP, beloep = 567)
                            ),
                            uttakGrad = 50, // => gradert
                            maanedligBeloep = 678 // skal mappes til gradertUttakBeloep
                        )
                    ),
                    privatAfp = listOf(
                        SimulertPrivatAfp(
                            alderAar = 66,
                            beloep = 789,
                            kompensasjonstillegg = 123,
                            kronetillegg = 5,
                            livsvarig = 93,
                            maanedligBeloep = 100
                        )
                    ),
                    pre2025OffentligAfp = SimulertPre2025OffentligAfp(
                        alderAar = 62,
                        totaltAfpBeloep = 890,
                        tidligereArbeidsinntekt = 123,
                        grunnbeloep = 456,
                        sluttpoengtall = 7.8,
                        trygdetid = 30,
                        poengaarTom1991 = 10,
                        poengaarFom1992 = 20,
                        grunnpensjon = 567,
                        tilleggspensjon = 678,
                        afpTillegg = 789,
                        saertillegg = 890,
                        afpGrad = 80,
                        afpAvkortetTil70Prosent = false
                    ),
                    livsvarigOffentligAfp = listOf(
                        SimulertLivsvarigOffentligAfp(
                            alderAar = 63,
                            beloep = 901,
                            maanedligBeloep = 100
                        )
                    ),
                    pensjonBeholdningPeriodeListe = listOf(
                        SimulertPensjonBeholdningPeriode(
                            pensjonBeholdning = 2.3,
                            garantipensjonBeholdning = 3.4,
                            garantitilleggBeholdning = 4.5,
                            datoFom = LocalDate.of(2022, 2, 1),
                            garantipensjonNivaa = SimulertGarantipensjonNivaa(
                                beloep = 5.6,
                                satsType = "sats1",
                                sats = 6.7,
                                anvendtTrygdetid = 40
                            )
                        )
                    ),
                    harUttak = true,
                    primaerTrygdetid = Trygdetid(kapittel19 = 0, kapittel20 = 39),
                    opptjeningGrunnlagListe = listOf(OpptjeningGrunnlag(aar = 1999, pensjonsgivendeInntekt = 1002)),
                    opptjeningListe = listOf(SimulertOpptjening(kalenderAar = 1999, pensjonsgivendeInntekt = 1002))
                ),
                alternativ = SimulertAlternativ(
                    gradertUttakAlder = SimulertUttakAlder(
                        alder = Alder(aar = 66, maaneder = 2),
                        uttakDato = LocalDate.of(2023, 3, 1),
                    ),
                    uttakGrad = UttakGradKode.P_20,
                    heltUttakAlder = SimulertUttakAlder(
                        alder = Alder(aar = 67, maaneder = 0),
                        uttakDato = LocalDate.of(2024, 1, 1),
                    ),
                    resultStatus = SimulatorResultStatus.GOOD
                )
            )
    }
}