package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import java.time.LocalDate

class KlpTjenestepensjonServiceTest : ShouldSpec({

    val client = mockk<KlpTjenestepensjonClientFra2025>(relaxed = true)
    val service = KlpTjenestepensjonService(client)

    beforeTest {
        clearMocks(client)
        every { client.service() } returns EgressService.KLP
    }

    should("ikke inkludere betinget tjenestepensjon og offentlig AFP i simulering") {
        val spec = spec()
        every { client.simuler(spec, tpNummer = "4082") } returns success()

        val result = service.simuler(spec, tpNummer = "4082")

        result.isSuccess shouldBe true
        with(result.getOrNull().shouldNotBeNull()) {
            betingetTjenestepensjonErInkludert shouldBe false
            utbetalingsperioder shouldHaveSize 2
        }
    }

    should("ikke simulere med betinget tjenestepensjon fra KLP") {
        val spec = spec()
        every { client.simuler(spec, tpNummer = "3010") } returns success()

        val result = service.simuler(spec, tpNummer = "3010")

        result.isSuccess shouldBe true
        result.getOrNull().shouldNotBeNull().betingetTjenestepensjonErInkludert shouldBe false
    }
}) {
    private companion object {

        private fun spec() =
            SimulerOffentligTjenestepensjonFra2025SpecV1(
                pid = "12345678910",
                foedselsdato = LocalDate.of(1963, 2, 5),
                uttaksdato = LocalDate.of(2025, 3, 1),
                sisteInntekt = 500000,
                aarIUtlandetEtter16 = 0,
                brukerBaOmAfp = true,
                epsPensjon = false,
                eps2G = false,
                fremtidigeInntekter = emptyList(),
                erApoteker = false
            )

        private fun success(): Result<SimulertTjenestepensjon> =
            Result.success(
                SimulertTjenestepensjon(
                    tpLeverandoer = "klp",
                    ordningsListe = listOf(Ordning(tpNummer = "3010")),
                    utbetalingsperioder = listOf(
                        Utbetalingsperiode(
                            fom = LocalDate.of(2026, 3, 1),
                            maanedligBelop = 3000,
                            ytelseType = TjenestepensjonYtelseType.SAERALDERSPAASLAG.kode
                        ),
                        Utbetalingsperiode(
                            fom = LocalDate.of(2027, 3, 1),
                            maanedligBelop = 4000,
                            ytelseType = TjenestepensjonYtelseType.OVERGANGSTILLEGG.kode
                        ),
                        Utbetalingsperiode(
                            fom = LocalDate.of(2026, 10, 1),
                            maanedligBelop = 2000,
                            ytelseType = TjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.kode
                        ),
                        Utbetalingsperiode(
                            fom = LocalDate.of(2026, 11, 1),
                            maanedligBelop = 2000,
                            ytelseType = TjenestepensjonYtelseType.OFFENTLIG_AFP.kode
                        ),
                    ),
                    erSisteOrdning = true,
                    betingetTjenestepensjonErInkludert = true
                )
            )
    }
}
