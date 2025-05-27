package no.nav.pensjon.simulator.inntekt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.satstabeller.SatsResultat
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.opptjening.SisteLignetInntekt
import no.nav.pensjon.simulator.regel.client.RegelClient
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class InntektServiceTest : FunSpec({

    val service = InntektService(
        lignetInntektService = arrangeInntekt(),
        regelClient = arrangeGrunnbeloep(),
        time = { LocalDate.of(2025, 1, 1) }
    )

    test("hentSisteLignetInntekt should return inntekt") {
        service.hentSisteLignetInntekt(pid) shouldBe 321000
    }

    test("hentSisteMaanedsInntektOver1G should return amount higher than 1G if harInntektSisteMaanedOver1G is true") {
        service.hentSisteMaanedsInntektOver1G(
            harInntektSisteMaanedOver1G = true
        ) shouldBeGreaterThan (GRUNNBELOEP / MAANEDER_PER_AAR).toInt()
    }

    test("hentSisteMaanedsInntektOver1G should return amount between 0 and 1G if harInntektSisteMaanedOver1G is false") {
        val result = service.hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G = false)

        result shouldBeLessThan (GRUNNBELOEP / MAANEDER_PER_AAR).toInt()
        result shouldBeGreaterThan 0
    }
})

private const val GRUNNBELOEP = 123000.0

private fun arrangeInntekt(): SisteLignetInntekt =
    mockk<SisteLignetInntekt>().also {
        every {
            it.hentSisteLignetInntekt(pid)
        } returns Inntekt(
            aarligBeloep = 321000,
            fom = LocalDate.of(2025, 1, 1)
        )
    }

private fun arrangeGrunnbeloep(): RegelClient =
    mockk<RegelClient>().also {
        every {
            it.fetchGrunnbeloepListe(dato = LocalDate.of(2025, 1, 1))
        } returns SatsResponse().apply {
            satsResultater = listOf(SatsResultat().apply { verdi = GRUNNBELOEP })
        }
    }
