package no.nav.pensjon.simulator.tjenestepensjon.forhold.client.tp.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Korresponderer med Map<"forhold", Boolean> i tp
 * (github.com/navikt/tp/.../samhandling/tp/controller/TjenestepensjonController.kt)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TpLeverandoerTilknytningResult(val forhold: Boolean)