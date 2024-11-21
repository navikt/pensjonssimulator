package no.nav.pensjon.simulator.ufoere

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.UtbetalingsgradUT
import org.springframework.stereotype.Service
import no.nav.pensjon.simulator.ufoere.client.UfoeretrygdUtbetalingClient

@Service
class UfoeretrygdUtbetalingService(private val client: UfoeretrygdUtbetalingClient) {

    fun getUtbetalingGradListe(penPersonId: Long): List<UtbetalingsgradUT> =
        client.fetchUtbetalingsgradListe(penPersonId)
}
