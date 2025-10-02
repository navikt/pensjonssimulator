package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.SIMULER_KLP
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpOrdningStoettesIkkeException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025ServiceUnitTest.Companion.dummyRequest
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import java.time.LocalDate

class KLPTjenestepensjonServiceUnitTest : FunSpec({

    val featureToggleService = mockk<FeatureToggleService>(relaxed = true)
    val klpClient = mockk<KLPTjenestepensjonClientFra2025>(relaxed = true)

    val service = KLPTjenestepensjonService(
        client = klpClient,
        featureToggleService = featureToggleService,
    )

    beforeTest {
        clearMocks(featureToggleService, klpClient)
        every { klpClient.service() } returns EgressService.KLP
    }

    test("simulering skal ikke skje naar feature toggle er av") {
        val req = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(SIMULER_KLP) } returns false

        val res: Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> = service.simuler(req, "4082")

        res.isFailure.shouldBeTrue()
        (res.exceptionOrNull() is TpOrdningStoettesIkkeException).shouldBeTrue()
    }

    test("BTP og OFTP skal ikke inkluderes i simulering") {
        val req = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(SIMULER_KLP) } returns true
        every { klpClient.simuler(req, "4082") } returns dummyResult()

        val res = service.simuler(req, "4082")

        res.isSuccess.shouldBeTrue()
        val tp = res.getOrNull().shouldNotBeNull()
        tp.betingetTjenestepensjonErInkludert shouldBe false
        tp.utbetalingsperioder.size shouldBe 2
    }

    test("ikke simuler med BTP fra klp") {
        val req = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(SIMULER_KLP) } returns true
        every { klpClient.simuler(req, "3010") } returns dummyResult()

        val res = service.simuler(req, "3010")

        res.isSuccess.shouldBeTrue()
        val tp = res.getOrNull().shouldNotBeNull()
        tp.betingetTjenestepensjonErInkludert shouldBe false
    }
}) {

    companion object {
        fun dummyResult(): Result<SimulertTjenestepensjon> =
            Result.success(
                SimulertTjenestepensjon(
                    tpLeverandoer = "klp",
                    ordningsListe = listOf(Ordning("3010")),
                    utbetalingsperioder = listOf(
                        Utbetalingsperiode(LocalDate.parse("2026-03-01"), 3000, "SAERALDERSPAASLAG"),
                        Utbetalingsperiode(LocalDate.parse("2027-03-01"), 4000, "OT6370"),
                        Utbetalingsperiode(LocalDate.parse("2026-10-01"), 2000, "BTP"),
                        Utbetalingsperiode(LocalDate.parse("2026-11-01"), 2000, "OAFP"),
                    ),
                    erSisteOrdning = true,
                    betingetTjenestepensjonErInkludert = true
                )
            )
    }
}
