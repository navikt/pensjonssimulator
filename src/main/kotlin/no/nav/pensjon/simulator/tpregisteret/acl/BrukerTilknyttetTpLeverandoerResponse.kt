package no.nav.pensjon.simulator.tpregisteret.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BrukerTilknyttetTpLeverandoerResponse(val forhold: Boolean)
