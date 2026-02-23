package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent

data class HentStillingsprosentListeResponse(
    val stillingsprosentListe: List<Stillingsprosent> = emptyList()
)