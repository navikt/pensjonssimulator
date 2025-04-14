package no.nav.pensjon.simulator.regel.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.HentGrunnbelopListeRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse

class GrunnbeloepServiceTest  : FunSpec({

    val mockRegelClient = mockk<GenericRegelClient>()
    val service = GrunnbeloepService(mockRegelClient)

    test("should return current year's grunnbeløp as int") {
        val expectedValue = 123456.5

        val mockResponse = SatsResponse().apply {
            satsResultater = listOf(SatsResultat().apply {
                verdi = expectedValue
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

        val result = service.hentAaretsGrunnbeloep()
        result shouldBe expectedValue.toInt()
    }
})