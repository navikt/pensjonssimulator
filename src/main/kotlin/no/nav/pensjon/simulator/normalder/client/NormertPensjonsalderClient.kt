package no.nav.pensjon.simulator.normalder.client

import no.nav.pensjon.simulator.normalder.Aldersgrenser

interface NormertPensjonsalderClient {

    fun fetchNormalderListe(): List<Aldersgrenser>
}
