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
}