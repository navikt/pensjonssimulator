package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025ServiceUnitTest.Companion.dummySpec
import java.time.LocalDate

class StorebrandTjenestepensjonServiceTest : FunSpec({

    val client = mockk<TjenestepensjonFra2025Client>()
    val featureToggleService = mockk<FeatureToggleService>()
    val service = StorebrandTjenestepensjonService(client, featureToggleService)

    beforeTest {
        clearMocks(client, featureToggleService)
        every { client.service } returns EgressService.STOREBRAND
    }

    test("simulerer og filtrerer offentlig AFP fra Storebrand") {
        val spec = dummySpec(foedselsdato = "1963-02-05", afpErForespurt = true)
        every { featureToggleService.isEnabled(FEATURE_TOGGLE) } returns true
        every { client.simuler(spec, TP_NUMMER) } returns success()

        val result = service.simuler(spec, TP_NUMMER)

        result.isSuccess shouldBe true
        with(result.getOrNull().shouldNotBeNull()) {
            tpLeverandoer shouldBe EgressService.STOREBRAND
            tpNummer shouldBe TP_NUMMER
            ordningsListe shouldHaveSize 1
            utbetalingsperioder shouldHaveSize 1
            utbetalingsperioder.single().maanedsBeloep shouldBe 3000
            betingetTjenestepensjonErInkludert shouldBe true
            serviceData shouldBe listOf("request", "response")
        }
    }

    test("simulerer ikke når feature toggle er av") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { featureToggleService.isEnabled(FEATURE_TOGGLE) } returns false

        val result = service.simuler(spec, TP_NUMMER)

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<TjenestepensjonSimuleringException>()
        verify(exactly = 0) { client.simuler(any(), any()) }
    }

    test("returnerer ikke siste ordning-feil") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { featureToggleService.isEnabled(FEATURE_TOGGLE) } returns true
        every { client.simuler(spec, TP_NUMMER) } returns success(erSisteOrdning = false)

        val result = service.simuler(spec, TP_NUMMER)

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<IkkeSisteOrdningException>()
    }

    test("returnerer feil for tom simulering") {
        val spec = dummySpec(foedselsdato = "1963-02-05")
        every { featureToggleService.isEnabled(FEATURE_TOGGLE) } returns true
        every { client.simuler(spec, TP_NUMMER) } returns success(utbetalingsperioder = emptyList())

        val result = service.simuler(spec, TP_NUMMER)

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<TomSimuleringFraTpOrdningException>()
    }
}) {
    private companion object {
        const val FEATURE_TOGGLE = "TPP-231_SIMULER_STOREBRAND"
        const val TP_NUMMER = "4081"

        fun success(
            erSisteOrdning: Boolean = true,
            utbetalingsperioder: List<Utbetalingsperiode> = listOf(
                Utbetalingsperiode(
                    fom = LocalDate.of(2026, 3, 1),
                    maanedligBelop = 1000,
                    ytelseType = "OAFP"
                ),
                Utbetalingsperiode(
                    fom = LocalDate.of(2026, 3, 1),
                    maanedligBelop = 3000,
                    ytelseType = "PAASLAG"
                )
            )
        ) =
            Result.success(
                SimulertTjenestepensjon(
                    tpLeverandoer = "Storebrand",
                    ordningsListe = listOf(Ordning(TP_NUMMER)),
                    utbetalingsperioder = utbetalingsperioder,
                    aarsakIngenUtbetaling = listOf("Ikke stoettet: SAERALDERSPAASLAG"),
                    betingetTjenestepensjonErInkludert = true,
                    serviceData = listOf("request", "response"),
                    erSisteOrdning = erSisteOrdning
                )
            )
    }
}
