package no.nav.pensjon.simulator.beholdning.client

import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdning
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec

interface BeholdningClient {

    fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning
}
