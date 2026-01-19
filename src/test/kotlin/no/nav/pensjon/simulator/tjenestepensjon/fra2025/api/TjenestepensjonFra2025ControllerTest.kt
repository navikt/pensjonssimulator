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
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.ResultatTypeDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Maanedsutbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.*
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Service
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

class TjenestepensjonFra2025ControllerTest : FunSpec({

    val traceAid = mockk<TraceAid>(relaxed = true)
    val service = mockk<TjenestepensjonFra2025Service>(relaxed = true)
    val controller = TjenestepensjonFra2025Controller(traceAid, service)

    beforeTest {
        clearMocks(service)
    }

    context("validering") {
        test("skal avvise spec med erApoteker=true") {
            val spec = dummySpec(erApoteker = true)

            val exception = shouldThrow<ResponseStatusException> {
                controller.simuler(spec)
            }

            exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            exception.reason shouldContain "Apoteker"
            verify(exactly = 0) { service.simuler(any()) }
        }

        test("skal avvise spec med foedselsdato før 1963-01-01") {
            val spec = dummySpec(foedselsdato = LocalDate.of(1962, 12, 31))

            val exception = shouldThrow<ResponseStatusException> {
                controller.simuler(spec)
            }

            exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            exception.reason shouldContain "Fødselsdato"
            verify(exactly = 0) { service.simuler(any()) }
        }

        test("skal akseptere spec med foedselsdato 1963-01-01") {
            val spec = dummySpec(foedselsdato = LocalDate.of(1963, 1, 1))
            every { service.simuler(any()) } returns Pair(emptyList(), Result.failure(RuntimeException("test")))

            shouldThrow<ResponseStatusException> {
                controller.simuler(spec)
            }

            verify(exactly = 1) { service.simuler(any()) }
        }
    }

    context("vellykket simulering") {
        test("returnerer SUCCESS med simuleringsresultat") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(
                listOf("3010"),
                Result.success(dummySimulertTjenestepensjon())
            )

            val result = controller.simuler(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDto.SUCCESS
            with(result.simuleringsResultat.shouldNotBeNull()) {
                tpLeverandoer shouldBe "Statens Pensjonskasse"
                tpNummer shouldBe "3010"
                utbetalingsperioder.size shouldBe 1
                utbetalingsperioder[0].maanedligBeloep shouldBe 5000
                betingetTjenestepensjonErInkludert shouldBe false
            }
            result.relevanteTpOrdninger shouldBe listOf("3010")
        }
    }

    context("exception handling") {
        test("BrukerErIkkeMedlemException returnerer BRUKER_ER_IKKE_MEDLEM_HOS_TP_ORDNING") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("3010"), Result.failure(BrukerErIkkeMedlemException()))

            val result = controller.simuler(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDto.BRUKER_ER_IKKE_MEDLEM_HOS_TP_ORDNING
        }

        test("TpOrdningStoettesIkkeException returnerer TP_ORDNING_ER_IKKE_STOTTET") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("9999"), Result.failure(TpOrdningStoettesIkkeException("9999")))

            val result = controller.simuler(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDto.TP_ORDNING_ER_IKKE_STOTTET
        }

        test("TjenestepensjonSimuleringException returnerer TEKNISK_FEIL_FRA_TP_ORDNING") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("3010"), Result.failure(TjenestepensjonSimuleringException("Feil", "SPK")))

            val result = controller.simuler(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDto.TEKNISK_FEIL_FRA_TP_ORDNING
        }

        test("TomSimuleringFraTpOrdningException returnerer INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("3010"), Result.failure(TomSimuleringFraTpOrdningException("SPK")))

            val result = controller.simuler(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDto.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING
        }

        test("IkkeSisteOrdningException returnerer INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("3010"), Result.failure(IkkeSisteOrdningException("SPK")))

            val result = controller.simuler(spec)

            result.simuleringsResultatStatus.resultatType shouldBe ResultatTypeDto.INGEN_UTBETALINGSPERIODER_FRA_TP_ORDNING
        }

        test("TpregisteretException kaster ResponseStatusException med INTERNAL_SERVER_ERROR") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("3010"), Result.failure(TpregisteretException("Feil fra tpregisteret")))

            val exception = shouldThrow<ResponseStatusException> {
                controller.simuler(spec)
            }

            exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        }

        test("ukjent exception kaster ResponseStatusException med INTERNAL_SERVER_ERROR") {
            val spec = dummySpec()
            every { service.simuler(any()) } returns Pair(listOf("3010"), Result.failure(RuntimeException("Ukjent feil")))

            val exception = shouldThrow<ResponseStatusException> {
                controller.simuler(spec)
            }

            exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
}) {
    companion object {
        fun dummySpec(
            foedselsdato: LocalDate = LocalDate.of(1963, 2, 5),
            erApoteker: Boolean = false
        ) = SimulerOffentligTjenestepensjonFra2025SpecV1(
            pid = "12345678910",
            foedselsdato = foedselsdato,
            uttaksdato = LocalDate.of(2025, 3, 1),
            sisteInntekt = 500000,
            aarIUtlandetEtter16 = 0,
            brukerBaOmAfp = false,
            epsPensjon = false,
            eps2G = false,
            fremtidigeInntekter = null,
            erApoteker = erApoteker
        )

        fun dummySimulertTjenestepensjon() = SimulertTjenestepensjonMedMaanedsUtbetalinger(
            tpLeverandoer = "Statens Pensjonskasse",
            tpNummer = "3010",
            ordningsListe = listOf(Ordning("3010")),
            utbetalingsperioder = listOf(
                Maanedsutbetaling(
                    fraOgMedDato = LocalDate.of(2025, 3, 1),
                    fraOgMedAlder = Alder(62, 0),
                    maanedsBeloep = 5000
                )
            ),
            betingetTjenestepensjonErInkludert = false
        )
    }
}
