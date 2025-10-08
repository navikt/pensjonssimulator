package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.PEN_715_SIMULER_SPK
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025ServiceUnitTest.Companion.dummyRequest
import java.time.LocalDate

class SpkTjenestepensjonServiceUnitTest : FunSpec({

    val client = mockk<SpkTjenestepensjonClientFra2025>(relaxed = true)
    val featureToggleService = mockk<FeatureToggleService>(relaxed = true)

    val service = SpkTjenestepensjonService(
        client = client,
        featureToggleService = featureToggleService
    )

    beforeTest {
        clearMocks(client, featureToggleService)
        every { client.service() } returns EgressService.SPK
    }

    test("simuler gruppering og sortering av tjenestepensjon fra spk") {
        val req = dummyRequest("1963-02-05")
        every { client.simuler(req, "3010") } returns dummyResult()
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true

        val res: Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> = service.simuler(req, "3010")

        res.isSuccess.shouldBeTrue()
        val tp = res.getOrNull().shouldNotBeNull()
        tp.tpLeverandoer shouldBe SpkMapper.PROVIDER_FULLT_NAVN
        tp.betingetTjenestepensjonErInkludert.shouldBeFalse()
        tp.ordningsListe shouldHaveSize 1
        tp.utbetalingsperioder shouldHaveSize 2

        tp.utbetalingsperioder[0].fraOgMedDato shouldBe LocalDate.parse("2026-01-01")
        tp.utbetalingsperioder[0].maanedsBeloep shouldBe 3000
        tp.utbetalingsperioder[0].fraOgMedAlder.aar shouldBe 62
        tp.utbetalingsperioder[0].fraOgMedAlder.maaneder shouldBe 10

        tp.utbetalingsperioder[1].fraOgMedDato shouldBe LocalDate.parse("2026-03-01")
        tp.utbetalingsperioder[1].maanedsBeloep shouldBe 7000
        tp.utbetalingsperioder[1].fraOgMedAlder.aar shouldBe 63
        tp.utbetalingsperioder[1].fraOgMedAlder.maaneder shouldBe 0
    }

    test("simuler med BTP fra spk") {
        val req = dummyRequest("1963-02-05")
        every { client.simuler(req, "3010") } returns dummyResult(inkluderBTP = true)
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true

        val res = service.simuler(req, "3010")

        res.isSuccess.shouldBeTrue()
        val tp = res.getOrNull().shouldNotBeNull()
        tp.betingetTjenestepensjonErInkludert.shouldBeTrue()
    }

    test("afp fjernes fra utbetalingsperioder fra spk") {
        val req = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true
        every { client.simuler(req, "3010") } returns Result.success(
            SimulertTjenestepensjon(
                tpLeverandoer = "spk",
                ordningsListe = listOf(Ordning("3010")),
                utbetalingsperioder = listOf(
                    Utbetalingsperiode(
                        fom = LocalDate.parse("2026-03-01"),
                        maanedligBelop = 3000,
                        ytelseType = "OAFP"
                    ),
                ),
                betingetTjenestepensjonErInkludert = false,
                erSisteOrdning = true
            )
        )

        val res = service.simuler(req, "3010")

        res.isSuccess.shouldBeTrue()
        val tp = res.getOrNull().shouldNotBeNull()
        tp.tpLeverandoer shouldBe SpkMapper.PROVIDER_FULLT_NAVN
        tp.betingetTjenestepensjonErInkludert.shouldBeFalse()
        tp.ordningsListe shouldHaveSize 1
        tp.utbetalingsperioder.shouldBeEmpty()
    }

    test("simulering skal ikke gjoeres if feature toggle er av") {
        val req = dummyRequest("1963-02-05")
        every { client.simuler(req, "3010") } returns dummyResult(inkluderBTP = true)
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns false

        val res = service.simuler(req, "3010")

        res.isFailure.shouldBeTrue()
        val ex = res.exceptionOrNull().shouldNotBeNull()
        (ex is TjenestepensjonSimuleringException).shouldBeTrue()
        ex.message!!.contains("Simulering av tjenestepensjon hos SPK er slått av").shouldBeTrue()
    }

    test("result skal vaere failure hvis ikke siste ordning") {
        val req = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true
        every { client.simuler(req, "3010") } returns Result.success(
            SimulertTjenestepensjon(
                tpLeverandoer = "spk",
                ordningsListe = listOf(Ordning("3010")),
                utbetalingsperioder = listOf(
                    Utbetalingsperiode(
                        fom = LocalDate.parse("2026-03-01"),
                        maanedligBelop = 3000,
                        ytelseType = "SAERALDERSPAASLAG"
                    ),
                ),
                betingetTjenestepensjonErInkludert = false,
                erSisteOrdning = false
            )
        )

        val res = service.simuler(req, "3010")

        res.isFailure.shouldBeTrue()
    }

    test("result should be failure with IkkeSisteOrdningException if not the last arrangement") {
        val req = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true
        every { client.simuler(req, "3010") } returns Result.success(
            SimulertTjenestepensjon(
                tpLeverandoer = "spk",
                ordningsListe = listOf(Ordning("3010")),
                utbetalingsperioder = emptyList(),
                betingetTjenestepensjonErInkludert = false,
                erSisteOrdning = false
            )
        )

        val res = service.simuler(req, "3010")

        res.isFailure.shouldBeTrue()
        val ex = res.exceptionOrNull().shouldNotBeNull()
        (ex is IkkeSisteOrdningException).shouldBeTrue()
        (ex as IkkeSisteOrdningException).tpOrdning shouldBe "SPK"
    }
}) {

    companion object {
        fun dummyResult(inkluderBTP: Boolean = false): Result<SimulertTjenestepensjon> =
            Result.success(
                SimulertTjenestepensjon(
                    tpLeverandoer = "spk",
                    ordningsListe = listOf(Ordning("3010")),
                    utbetalingsperioder = listOf(
                        Utbetalingsperiode(
                            fom = LocalDate.parse("2026-03-01"),
                            maanedligBelop = 3000,
                            ytelseType = "SAERALDERSPAASLAG"
                        ),
                        Utbetalingsperiode(
                            fom = LocalDate.parse("2026-03-01"),
                            maanedligBelop = 4000,
                            ytelseType = "OT6370"
                        ),
                        Utbetalingsperiode(
                            fom = LocalDate.parse("2026-01-01"),
                            maanedligBelop = 1000,
                            ytelseType = "PAASLAG"
                        ),
                        Utbetalingsperiode(
                            fom = LocalDate.parse("2026-01-01"),
                            maanedligBelop = 2000,
                            ytelseType = "APOF2020"
                        ),
                    ),
                    betingetTjenestepensjonErInkludert = inkluderBTP,
                    erSisteOrdning = true
                )
            )
    }
}
