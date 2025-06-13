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
        regelClient = arrangeGrunnbeloep(),
        time = { LocalDate.of(2025, 1, 1) }
    )

    test("naavaerendeGrunnbeloep should return nåværende grunnbeløp") {
        service.naavaerendeGrunnbeloep() shouldBe 123000
    }
})

private fun arrangeGrunnbeloep(): RegelClient =
    mockk<RegelClient>().also {
        every {
            it.fetchGrunnbeloepListe(dato = LocalDate.of(2025, 1, 1))
        } returns SatsResponse().apply {
            satsResultater = listOf(SatsResultat().apply { verdi = 123000.0 })
        }
    }
