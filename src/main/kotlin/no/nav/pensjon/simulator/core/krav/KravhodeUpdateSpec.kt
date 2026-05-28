package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec

// Corresponds to OppdaterKravhodeForForsteKnekkpunktRequest
/**
 * Spesifikasjon (parametre) for oppdatering av kravhode.
 */
data class KravhodeUpdateSpec(
    val kravhode: Kravhode,
    val simuleringSpec: SimuleringSpec,
    val erFoerstegangsberegning: Boolean
)
