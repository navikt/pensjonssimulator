package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent

data class HentStillingsprosentListeResponse(
        @get:JsonValue val stillingsprosentListe: List<Stillingsprosent> = emptyList()
)