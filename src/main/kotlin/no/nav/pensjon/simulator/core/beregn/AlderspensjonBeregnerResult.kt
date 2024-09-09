package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.VilkarsprovOgBeregnAlderspensjonResponse
data class AlderspensjonBeregnerResult(
    val beregningsresultater: MutableList<AbstraktBeregningsResultat>,
    //val pensjonsbeholdningPerioder: MutableList<Ap2025PensjonsbeholdningPeriode>
    val pensjonsbeholdningPerioder: MutableList<BeholdningPeriode>
)
