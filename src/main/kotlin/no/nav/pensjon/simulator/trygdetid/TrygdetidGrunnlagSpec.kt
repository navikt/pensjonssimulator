package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.time.LocalDate

// PEN: no.nav.service.pensjon.simulering.abstractsimulerapfra2011.SettTrygdetidsgrunnlagRequest
data class TrygdetidGrunnlagSpec(
    val persongrunnlag: Persongrunnlag,
    val utlandAntallAar: Int?,
    val tom: LocalDate?,
    val forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
    val simuleringSpec: SimuleringSpec
)