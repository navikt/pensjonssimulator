package no.nav.pensjon.simulator.regel.client

import no.nav.pensjon.simulator.core.domain.regler.to.HentGrunnbelopListeRequest
import no.nav.pensjon.simulator.core.domain.regler.to.SatsResponse
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class GrunnbeloepService(
    val regelService: GenericRegelClient
) {

    fun hentAaretsGrunnbeloep() : Int {
        val date: Date = LocalDate.now().toNorwegianDateAtNoon()

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

    fun hentSisteMaanedsInntektOver1G(inntektSisteMaanedOver1G: Boolean) : Int {
        return if (inntektSisteMaanedOver1G){
            hentAaretsGrunnbeloep() * TILFELDIG_ANTALL_G_STOERRE_ENN_EN / MAANEDER_I_AAR
        } else {
            TILFELDIG_MAANEDS_INNTEKT_STOERRE_ENN_NULL
        }
    }

    companion object {
        const val MAANEDER_I_AAR = 12
        const val TILFELDIG_MAANEDS_INNTEKT_STOERRE_ENN_NULL = 42
        const val TILFELDIG_ANTALL_G_STOERRE_ENN_EN = 2
    }
}