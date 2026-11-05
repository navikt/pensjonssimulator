package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.RelevantOrdningV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.ResultatTypeDtoV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.SimulerOffentligTjenestepensjonFra2025SpecV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2.UtbetalingPerAlderV2
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Maanedsutbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.*
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Service
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

class TjenestepensjonFra2025ControllerV2Test : FunSpec({

    val traceAid = mockk<TraceAid>(relaxed = true)
    val service = mockk<TjenestepensjonFra2025Service>()
    val controller = TjenestepensjonFra2025Controller(traceAid, service)
    val tpOrdninger = listOf(
        Tjenestepensjonsforhold("3010", "Statens Pensjonskasse", LocalDate.of(2025, 1, 1)),
        Tjenestepensjonsforhold("4081", "Storebrand", null)
    )
    val relevanteTpOrdninger = listOf(
        RelevantOrdningV2("Statens Pensjonskasse", "3010"),
        RelevantOrdningV2("Storebrand", "4081")
    )

    beforeTest {
        clearMocks(service, traceAid)
    }

    afterTest {
        verify(exactly = 1) { traceAid.begin() }
        verify(exactly = 1) { traceAid.end() }
    }

    context("validering") {
        test("skal avvise spec med erApoteker=true") {
            val exception = shouldThrow<ResponseStatusException> {
                controller.simulerV2(dummySpec(erApoteker = true))
            }

            exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            exception.reason shouldContain "Apoteker"
            verify(exactly = 0) { service.simuler(any()) }
        }

        test("skal avvise spec med foedselsdato før 1963-01-01") {
            val exception = shouldThrow<ResponseStatusException> {
                controller.simulerV2(dummySpec(foedselsdato = LocalDate.of(1962, 12, 31)))
            }

            exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            exception.reason shouldContain "Fødselsdato"
            verify(exactly = 0) { service.simuler(any()) }
        }

        test("skal akseptere spec med foedselsdato 1963-01-01") {
            every { service.simuler(any()) } returns (tpOrdninger to Result.success(dummySimulertTjenestepensjon()))

            val result = controller.simulerV2(dummySpec(foedselsdato = LocalDate.of(1963, 1, 1)))

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDtoV2.SUCCESS
            verify(exactly = 1) {
                service.simuler(match { it.foedselsdato == LocalDate.of(1963, 1, 1) })
            }
        }
    }

    context("vellykket simulering") {
        test("returnerer SUCCESS med simuleringsresultat og relevante ordninger med navn og nummer") {
            val spec = dummySpec()
            val expectedSpec = OffentligTjenestepensjonFra2025SimuleringSpec(
                pid = Pid(spec.pid),
                foedselsdato = spec.foedselsdato,
                uttaksdato = spec.uttaksdato,
                sisteInntekt = 500000,
                utlandAntallAar = 0,
                afpErForespurt = false,
                epsHarPensjon = false,
                epsHarInntektOver2G = false,
                fremtidigeInntekter = emptyList(),
                gjelderApoteker = false
            )
            every { service.simuler(expectedSpec) } returns
                    (tpOrdninger to Result.success(dummySimulertTjenestepensjon()))

            val result = controller.simulerV2(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDtoV2.SUCCESS
            result.simuleringsResultatStatus.feilmelding shouldBe null
            with(result.simuleringsResultat.shouldNotBeNull()) {
                tpLeverandoer shouldBe "Statens Pensjonskasse"
                tpNummer shouldBe "3010"
                utbetalingsperioder shouldBe listOf(UtbetalingPerAlderV2(Alder(62, 0), null, 5000))
                betingetTjenestepensjonErInkludert shouldBe false
            }
            result.relevanteTpOrdninger shouldBe relevanteTpOrdninger
            result.serviceData shouldBe listOf("request", "response")
            verify(exactly = 1) { service.simuler(expectedSpec) }
        }
    }

    context("exception handling") {
        listOf(
            Triple(
                BrukerErIkkeMedlemException(),
                ResultatTypeDtoV2.BRUKER_ER_IKKE_MEDLEM_HOS_TP_ORDNING,
                "Bruker er ikke medlem av en offentlig tjenestepensjonsordning"
            ),
            Triple(
                TpOrdningStoettesIkkeException("9999"),
                ResultatTypeDtoV2.TP_ORDNING_ER_IKKE_STOTTET,
                "9999 støtter ikke simulering av tjenestepensjon v2025"
            ),
            Triple(
                TjenestepensjonSimuleringException("Feil", "SPK"),
                ResultatTypeDtoV2.TEKNISK_FEIL_FRA_TP_ORDNING,
                "Feil ved simulering av tjenestepensjon Feil"
            ),
            Triple(
                TomSimuleringFraTpOrdningException("SPK"),
                ResultatTypeDtoV2.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING,
                "Simulering fra SPK inneholder ingen utbetalingsperioder"
            ),
            Triple(
                IkkeSisteOrdningException("SPK"),
                ResultatTypeDtoV2.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING,
                "Simulering fra SPK inneholder ingen utbetalingsperioder"
            )
        ).forEach { (exception, resultatType, feilmelding) ->
            test("${exception::class.simpleName} returnerer $resultatType") {
                every { service.simuler(any()) } returns (tpOrdninger to Result.failure(exception))

                val result = controller.simulerV2(dummySpec())

                result.simuleringsResultatStatus.resultatType shouldBe resultatType
                result.simuleringsResultatStatus.feilmelding shouldBe feilmelding
                result.simuleringsResultat shouldBe null
                result.relevanteTpOrdninger shouldBe relevanteTpOrdninger
            }
        }

        listOf(
            TpregisteretException("Feil fra tpregisteret"),
            RuntimeException("Ukjent feil")
        ).forEach { exception ->
            test("${exception::class.simpleName} kaster ResponseStatusException med INTERNAL_SERVER_ERROR") {
                every { service.simuler(any()) } returns (tpOrdninger to Result.failure(exception))

                val result = shouldThrow<ResponseStatusException> {
                    controller.simulerV2(dummySpec())
                }

                result.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                result.reason shouldBe exception.message
            }
        }
    }
}) {
    private companion object {
        fun dummySpec(
            foedselsdato: LocalDate = LocalDate.of(1963, 2, 5),
            erApoteker: Boolean = false
        ) = SimulerOffentligTjenestepensjonFra2025SpecV2(
            pid = "12345678910",
            foedselsdato = foedselsdato,
            uttaksdato = LocalDate.of(2025, 3, 1),
            sisteInntekt = 500000,
            aarIUtlandetEtter16 = 0,
            brukerBaOmAfp = false,
            epsPensjon = false,
            eps2G = false,
            fremtidigeInntekter = emptyList(),
            erApoteker = erApoteker
        )

        fun dummySimulertTjenestepensjon() = SimulertTjenestepensjonMedMaanedsUtbetalinger(
            tpLeverandoer = EgressService.SPK,
            tpNummer = "3010",
            ordningsListe = listOf(Ordning("3010")),
            utbetalingsperioder = listOf(
                Maanedsutbetaling(LocalDate.of(2025, 3, 1), Alder(62, 0), 5000)
            ),
            betingetTjenestepensjonErInkludert = false,
            serviceData = listOf("request", "response")
        )
    }
}