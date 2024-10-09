package no.nav.pensjon.simulator.tpregisteret

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BrukerTilknyttetTpLeverandoerResponse(val forhold: Boolean)
