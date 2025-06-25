package no.nav.pensjon.simulator.g

import no.nav.pensjon.simulator.regel.client.RegelClient
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GrunnbeloepService(
    private val regelClient: RegelClient,
    private val time: Time
) {
    fun grunnbeloep(dato: LocalDate): Int =
        regelClient.fetchGrunnbeloepListe(dato).satsResultater.first().verdi.toInt()

    fun naavaerendeGrunnbeloep(): Int =
        grunnbeloep(time.today())
}
