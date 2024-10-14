package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode

// Corresponds to OppdaterKravhodeForForsteKnekkpunktRequest
data class KravhodeUpdateSpec(
    val kravhode: Kravhode,
    val simulering: SimuleringSpec,
    val forrigeAlderspensjonBeregningResult: AbstraktBeregningsResultat?
)
