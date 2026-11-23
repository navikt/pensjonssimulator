package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat

data class AlderspensjonBeregnerResult(
    val beregningsresultater: MutableList<AbstraktBeregningsResultat>,
    val pensjonsbeholdningPerioder: List<BeholdningPeriode>
)