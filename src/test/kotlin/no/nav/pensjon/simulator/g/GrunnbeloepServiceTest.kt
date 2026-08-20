package no.nav.pensjon.simulator.g

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.regel.client.RegelClient
import java.time.LocalDate

class GrunnbeloepServiceTest : FunSpec({

    val service = GrunnbeloepService(
        regelClient = arrangeGrunnbeloep,
        time = { LocalDate.of(2025, 1, 1) }
    )

    test("naavaerendeGrunnbeloep should return nåværende grunnbeløp") {
        service.naavaerendeGrunnbeloep() shouldBe 123000
    }
})

private val arrangeGrunnbeloep: RegelClient =
    mockk {
        every {
            fetchGrunnbeloepListe()
        } returns SatsResponse().apply {
            satsResultater = listOf(SatsResultat().apply {
                fomLd = LocalDate.of(2024, 5, 1)
                tomLd = LocalDate.of(2025, 4, 30)
                verdi = 123000.0
            })
        }
    }