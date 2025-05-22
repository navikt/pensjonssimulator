package no.nav.pensjon.simulator.regel.client

import no.nav.pensjon.simulator.core.domain.regler.to.HentGrunnbelopListeRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.util.*

@Service
class GrunnbeloepService(
    private val regelService: GenericRegelClient,
    private val time: Time
) {
    fun hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G: Boolean): Int =
        if (harInntektSisteMaanedOver1G)
            hentAaretsGrunnbeloep() * TILFELDIG_ANTALL_G_STOERRE_ENN_EN / MAANEDER_PER_AAR
        else
            TILFELDIG_MAANEDSINNTEKT_STOERRE_ENN_NULL

    private fun hentAaretsGrunnbeloep(): Int {
        val date: Date = time.today().toNorwegianDateAtNoon()

        val response = regelService.makeRegelCall<SatsResponse, HentGrunnbelopListeRequest>(
            request = HentGrunnbelopListeRequest().apply {
                fom = date
                tom = date
            },
            responseClass = SatsResponse::class.java,
            serviceName = "hentGrunnbelopListe",
            map = null,
            sakId = null
        )

        return response.satsResultater.first().verdi.toInt()
    }

    private companion object {
        private const val TILFELDIG_MAANEDSINNTEKT_STOERRE_ENN_NULL = 42
        private const val TILFELDIG_ANTALL_G_STOERRE_ENN_EN = 2
    }
}
