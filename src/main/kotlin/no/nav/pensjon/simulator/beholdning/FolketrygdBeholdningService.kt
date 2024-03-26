package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import org.springframework.stereotype.Component

@Component

class FolketrygdBeholdningService(private val client: BeholdningClient) {

    fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning =
        client.simulerFolketrygdBeholdning(spec)
}
