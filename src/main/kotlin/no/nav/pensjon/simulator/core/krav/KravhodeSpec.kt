package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat

// Corresponds to OpprettKravhodeRequest
data class KravhodeSpec(
    val simulatorInput: SimuleringSpec,
    val forrigeAlderspensjonBeregningResult: AbstraktBeregningsResultat?,
    val grunnbeloep: Int
)
