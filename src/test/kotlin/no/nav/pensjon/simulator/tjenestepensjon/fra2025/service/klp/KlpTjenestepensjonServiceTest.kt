package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonFra2025Client
import java.time.LocalDate

class KlpTjenestepensjonServiceTest : ShouldSpec({

    val client: TjenestepensjonFra2025Client = mockk()
    val service = KlpTjenestepensjonService(client)

    beforeTest {
        clearMocks(client)
        every { client.leverandoerFulltNavn } returns EgressService.KLP.description
        every { client.leverandoerKortNavn } returns EgressService.KLP.shortName
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
            OffentligTjenestepensjonFra2025SimuleringSpec(
                pid = Pid("12345678910"),
                foedselsdato = LocalDate.of(1963, 2, 5),
                uttaksdato = LocalDate.of(2025, 3, 1),
                sisteInntekt = 500000,
                utlandAntallAar = 0,
                afpErForespurt = true,
                epsHarPensjon = false,
                epsHarInntektOver2G = false,
                fremtidigeInntekter = emptyList(),
                gjelderApoteker = false
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
