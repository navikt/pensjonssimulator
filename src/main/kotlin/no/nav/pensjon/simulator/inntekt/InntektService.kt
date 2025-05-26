package no.nav.pensjon.simulator.inntekt

import no.nav.pensjon.simulator.opptjening.SisteLignetInntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.regel.client.RegelClient
import no.nav.pensjon.simulator.tech.time.DateUtil
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service

@Service
class InntektService(
    private val lignetInntektService: SisteLignetInntekt,
    private val regelClient: RegelClient,
    private val time: Time
) {
    fun hentSisteLignetInntekt(pid: Pid): Int =
        lignetInntektService.hentSisteLignetInntekt(pid).aarligBeloep

    fun hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G: Boolean): Int =
        if (harInntektSisteMaanedOver1G)
            naavaerendeGrunnbeloep() * TILFELDIG_ANTALL_G_STOERRE_ENN_EN / DateUtil.MAANEDER_PER_AAR
        else
            TILFELDIG_MAANEDSINNTEKT_STOERRE_ENN_NULL

    private fun naavaerendeGrunnbeloep(): Int =
        regelClient.fetchGrunnbeloepListe(time.today()).satsResultater.first().verdi.toInt()

    private companion object {
        private const val TILFELDIG_MAANEDSINNTEKT_STOERRE_ENN_NULL = 42
        private const val TILFELDIG_ANTALL_G_STOERRE_ENN_EN = 2
    }
}
