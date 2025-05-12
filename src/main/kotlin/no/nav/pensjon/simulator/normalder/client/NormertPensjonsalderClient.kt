package no.nav.pensjon.simulator.normalder.client

import no.nav.pensjon.simulator.normalder.NormertPensjonsalder

interface NormertPensjonsalderClient {

    fun fetchNormAlderListe(): List<NormertPensjonsalder>
}
