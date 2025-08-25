package no.nav.pensjon.simulator.alderspensjon.spec

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate

class OffentligSimuleringstypeDeducerTest : FunSpec({

    val uttakFom = LocalDate.of(2030, 2, 1)
    val afpRettFom = LocalDate.of(2030, 1, 1)

    test("should deduce ALDER for førstegangsuttak uten AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = false))

        deducer.deduceSimuleringstype(pid, uttakFom, livsvarigOffentligAfpRettFom = null) shouldBe
                SimuleringTypeEnum.ALDER
    }

    test("should deduce ENDR_ALDER for endring uten AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = true))

        deducer.deduceSimuleringstype(pid, uttakFom, livsvarigOffentligAfpRettFom = null) shouldBe
                SimuleringTypeEnum.ENDR_ALDER
    }

    test("should deduce ALDER_MED_AFP_OFFENTLIG_LIVSVARIG for førstegangsuttak med AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = false))

        deducer.deduceSimuleringstype(pid, uttakFom, afpRettFom) shouldBe
                SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG
    }

    test("should deduce ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG for endring med AFP") {
        val deducer = OffentligSimuleringstypeDeducer(arrangeVedtak(harGjeldendeVedtak = true))

        deducer.deduceSimuleringstype(pid, uttakFom, afpRettFom) shouldBe
                SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
    }

    test("should throw exception for person med gjenlevenderettigheter") {
        val deducer = OffentligSimuleringstypeDeducer(
            vedtakService = arrangeVedtak(harGjeldendeVedtak = false, harGjenlevenderettighet = true)
        )

        shouldThrow<FeilISimuleringsgrunnlagetException> {
            deducer.deduceSimuleringstype(pid, uttakFom, afpRettFom)
        }.message shouldBe "Kan ikke simulere bruker med gjenlevenderettigheter"
    }
})

// Cannot use mockk with inline value class Pid
private fun arrangeVedtak(harGjeldendeVedtak: Boolean, harGjenlevenderettighet: Boolean = false): VedtakService =
    mock(VedtakService::class.java).apply {
        `when`(vedtakStatus(pid, uttakFom = LocalDate.of(2030, 2, 1))).thenReturn(
            VedtakStatus(harGjeldendeVedtak, harGjenlevenderettighet)
        )
    }
