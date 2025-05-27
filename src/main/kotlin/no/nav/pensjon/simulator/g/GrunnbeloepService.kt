package no.nav.pensjon.simulator.g

import no.nav.pensjon.simulator.regel.client.RegelClient
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service

@Service
class GrunnbeloepService(
    private val regelClient: RegelClient,
    private val time: Time
) {
    fun naavaerendeGrunnbeloep(): Int =
        regelClient.fetchGrunnbeloepListe(time.today()).satsResultater.first().verdi.toInt()
}
