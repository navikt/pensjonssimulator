package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

class SpkTjenestepensjonServiceTest : FunSpec({

    val client = mockk<SpkTjenestepensjonClientFra2025>(relaxed = true)
    val featureToggleService = mockk<FeatureToggleService>(relaxed = true)
    val service = SpkTjenestepensjonService(client, featureToggleService)

    beforeTest {
        clearMocks(client, featureToggleService)
        every { client.service() } returns EgressService.SPK
    }

    test("simuler gruppering og sortering av tjenestepensjon fra SPK") {
        val request = dummyRequest("1963-02-05")
        every { client.simuler(request, tpNummer = "3010") } returns dummyResult()
        every { featureToggleService.isEnabled(featureName = PEN_715_SIMULER_SPK) } returns true

        val result: Result<SimulertTjenestepensjonMedMaanedsUtbetalinger> = service.simuler(request, tpNummer = "3010")

        result.isSuccess shouldBe true
        with(result.getOrNull().shouldNotBeNull()) {
            tpLeverandoer shouldBe SpkMapper.PROVIDER_FULLT_NAVN
            betingetTjenestepensjonErInkludert shouldBe false
            ordningsListe shouldHaveSize 1
            utbetalingsperioder shouldHaveSize 2
            with(utbetalingsperioder[0]) {
                fraOgMedDato shouldBe LocalDate.parse("2026-01-01")
                maanedsBeloep shouldBe 3000
                fraOgMedAlder.aar shouldBe 62
                fraOgMedAlder.maaneder shouldBe 10
            }
            with(utbetalingsperioder[1]) {
                fraOgMedDato shouldBe LocalDate.parse("2026-03-01")
                maanedsBeloep shouldBe 7000
                fraOgMedAlder.aar shouldBe 63
                fraOgMedAlder.maaneder shouldBe 0
            }
        }
    }

    test("simuler med betinget tjenestepensjon fra SPK") {
        val request = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(featureName = PEN_715_SIMULER_SPK) } returns true
        every {
            client.simuler(spec = request, tpNummer = "3010")
        } returns dummyResult(inkluderBetingetTjenestepensjon = true)

        val result = service.simuler(request, "3010")

        result.isSuccess shouldBe true
        result.getOrNull().shouldNotBeNull().betingetTjenestepensjonErInkludert shouldBe true
    }

    test("AFP fjernes fra utbetalingsperioder fra SPK") {
        val request = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true
        every { client.simuler(request, "3010") } returns Result.success(
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

        val result = service.simuler(request, tpNummer = "3010")

        result.isSuccess shouldBe true
        with(result.getOrNull().shouldNotBeNull()) {
            tpLeverandoer shouldBe SpkMapper.PROVIDER_FULLT_NAVN
            betingetTjenestepensjonErInkludert shouldBe false
            ordningsListe shouldHaveSize 1
            utbetalingsperioder.shouldBeEmpty()
        }
    }

    test("simulering skal ikke gjoeres if feature toggle er av") {
        val request = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(featureName = PEN_715_SIMULER_SPK) } returns false
        every {
            client.simuler(spec = request, tpNummer = "3010")
        } returns dummyResult(inkluderBetingetTjenestepensjon = true)

        val result = service.simuler(request, tpNummer = "3010")

        with(result) {
            isFailure shouldBe true
            exceptionOrNull()
                .shouldBeInstanceOf<TjenestepensjonSimuleringException>()
                .message.contains("Simulering av tjenestepensjon hos SPK er slått av") shouldBe true
        }
    }

    test("result skal vaere failure hvis ikke siste ordning") {
        val request = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(featureName = PEN_715_SIMULER_SPK) } returns true
        every { client.simuler(spec = request, tpNummer = "3010") } returns Result.success(
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

        service.simuler(request, tpNummer = "3010").isFailure shouldBe true
    }

    test("result should be failure with IkkeSisteOrdningException if not the last arrangement") {
        val request = dummyRequest("1963-02-05")
        every { featureToggleService.isEnabled(PEN_715_SIMULER_SPK) } returns true
        every { client.simuler(request, "3010") } returns Result.success(
            SimulertTjenestepensjon(
                tpLeverandoer = "spk",
                ordningsListe = listOf(Ordning("3010")),
                utbetalingsperioder = emptyList(),
                betingetTjenestepensjonErInkludert = false,
                erSisteOrdning = false
            )
        )

        val result = service.simuler(request, tpNummer = "3010")

        with(result) {
            isFailure shouldBe true
            exceptionOrNull()
                .shouldBeInstanceOf<IkkeSisteOrdningException>()
                .tpOrdning shouldBe "SPK"
        }
    }
}) {
    companion object {
        fun dummyResult(inkluderBetingetTjenestepensjon: Boolean = false): Result<SimulertTjenestepensjon> =
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
                    betingetTjenestepensjonErInkludert = inkluderBetingetTjenestepensjon,
                    erSisteOrdning = true
                )
            )
    }
}
