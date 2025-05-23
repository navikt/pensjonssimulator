package no.nav.pensjon.simulator.regel.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.HentGrunnbelopListeRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate

class GrunnbeloepServiceTest : FunSpec({

    val regelService = mockk<GenericRegelClient>()
    val service = GrunnbeloepService(regelService, time = { LocalDate.of(2025, 1, 1) })

    test("hentSisteMaanedsInntektOver1G should return amount higher than 1G if inntektSisteMaanedOver1G is true") {
        val grunnbeloep = 100000.0
        arrangeRegelService(regelService, grunnbeloep)

        service.hentSisteMaanedsInntektOver1G(true) shouldBeGreaterThan (grunnbeloep / MAANEDER_PER_AAR).toInt()
    }

    test("hentSisteMaanedsInntektOver1G should return amount between 0 and 1G if inntektSisteMaanedOver1G is false") {
        val grunnbeloep = 110000.0
        arrangeRegelService(regelService, grunnbeloep)

        val result = service.hentSisteMaanedsInntektOver1G(false)

        result shouldBeLessThan (grunnbeloep / MAANEDER_PER_AAR).toInt()
        result shouldBeGreaterThan 0
    }
})

private fun arrangeRegelService(service: GenericRegelClient, grunnbeloep: Double) {
    every<SatsResponse> {
        service.makeRegelCall(
            request = any<HentGrunnbelopListeRequest>(),
            responseClass = SatsResponse::class.java,
            serviceName = "hentGrunnbelopListe",
            map = null,
            sakId = null
        )
    } returns SatsResponse().apply {
        satsResultater = listOf(SatsResultat().apply { verdi = grunnbeloep })
    }
}
