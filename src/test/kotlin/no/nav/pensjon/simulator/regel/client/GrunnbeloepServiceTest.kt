package no.nav.pensjon.simulator.regel.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.HentGrunnbelopListeRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.regel.client.GrunnbeloepService.Companion.MAANEDER_I_AAR

class GrunnbeloepServiceTest  : FunSpec({

    val mockRegelClient = mockk<GenericRegelClient>()
    val service = GrunnbeloepService(mockRegelClient)

    test("should return current year's grunnbeløp as int") {
        val grunnbeloep = 123456.5
        mock(mockRegelClient, grunnbeloep)

        val result = service.hentAaretsGrunnbeloep()
        result shouldBe grunnbeloep.toInt()
    }

    test("should return amount higher than 1G if inntektSisteMaanedOver1G is true") {
        val grunnbeloep = 100000.0
        mock(mockRegelClient, grunnbeloep)

        val result = service.hentSisteMaanedsInntektOver1G(true)
        result shouldBeGreaterThan (grunnbeloep / MAANEDER_I_AAR).toInt()
    }

    test("should return amount between 0 and 1G if inntektSisteMaanedOver1G is false") {
        val grunnbeloep = 100000.0
        mock(mockRegelClient, grunnbeloep)

        val result = service.hentSisteMaanedsInntektOver1G(false)
        result shouldBeLessThan (grunnbeloep / MAANEDER_I_AAR).toInt()
        result shouldBeGreaterThan 0
    }
})

private fun mock(
    mockRegelClient: GenericRegelClient,
    grunnbeloep: Double
) {
    val mockResponse = SatsResponse().apply {
        satsResultater = listOf(SatsResultat().apply {
            verdi = grunnbeloep
        })
    }

    every<SatsResponse> {
        mockRegelClient.makeRegelCall(
            any<HentGrunnbelopListeRequest>(),
            SatsResponse::class.java,
            "hentGrunnbelopListe",
            null,
            null
        )
    } returns mockResponse
}