package no.nav.pensjon.simulator.alderspensjon.spec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import java.time.LocalDate

class OffentligSimuleringstypeDeducerTest : ShouldSpec({

    val uttakFom = LocalDate.of(2030, 2, 1)
    val afpRettFom = LocalDate.of(2030, 1, 1)

    should("deduce ALDER for førstegangsuttak uten AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = false))

        deducer.deduceSimuleringstype(pid, uttakFom, livsvarigOffentligAfpRettFom = null) shouldBe
                SimuleringTypeEnum.ALDER
    }

    should("deduce ENDR_ALDER for endring uten AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = true))

        deducer.deduceSimuleringstype(pid, uttakFom, livsvarigOffentligAfpRettFom = null) shouldBe
                SimuleringTypeEnum.ENDR_ALDER
    }

    should("deduce ALDER_MED_AFP_OFFENTLIG_LIVSVARIG for førstegangsuttak med AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = false))

        deducer.deduceSimuleringstype(pid, uttakFom, afpRettFom) shouldBe
                SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG
    }

    should("deduce ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG for endring med AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = true))

        deducer.deduceSimuleringstype(pid, uttakFom, afpRettFom) shouldBe
                SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
    }
})

private fun arrangeVedtak(harGjeldendeVedtak: Boolean, harGjenlevenderettighet: Boolean = false): VedtakService =
    mockk<VedtakService> {
        every {
            vedtakStatus(any(), any())
        } returns VedtakStatus(harGjeldendeVedtak, harGjenlevenderettighet)
    }
