package no.nav.pensjon.simulator.inntekt

import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import org.springframework.stereotype.Service

@Service
class InntektService(
    private val inntektClient: InntektClient,
    private val grunnbeloepService: GrunnbeloepService
) {
    fun hentSisteLignetInntekt(pid: Pid): LoependeInntekt =
        inntektClient.fetchSistLignedeInntekt(pid)

    fun hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G: Boolean): Int =
        if (harInntektSisteMaanedOver1G)
            grunnbeloepService.naavaerendeGrunnbeloep() * TILFELDIG_ANTALL_G_STOERRE_ENN_EN / MAANEDER_PER_AAR
        else
            TILFELDIG_MAANEDSINNTEKT_STOERRE_ENN_NULL

    private companion object {
        private const val TILFELDIG_MAANEDSINNTEKT_STOERRE_ENN_NULL = 42
        private const val TILFELDIG_ANTALL_G_STOERRE_ENN_EN = 2
    }
}
