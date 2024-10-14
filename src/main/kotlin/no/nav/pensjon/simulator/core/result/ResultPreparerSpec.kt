package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.out.OutputLivsvarigOffentligAfp
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.OpprettOutputRequest
data class ResultPreparerSpec(
    val simuleringSpec: SimuleringSpec,
    val kravhode: Kravhode,
    val alderspensjonBeregningResultatListe: MutableList<AbstraktBeregningsResultat>,
    val privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat>,
    val forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
    val forrigePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat?,
    val pre2025OffentligAfpBeregningResultat: Simuleringsresultat?,
    val livsvarigOffentligAfpBeregningResultatListe: List<OutputLivsvarigOffentligAfp>?,
    val grunnbeloep: Int,
    val pensjonBeholdningPeriodeListe: List<BeholdningPeriode>,
    val outputSimulertBeregningsInformasjonForAllKnekkpunkter: Boolean,
    val sisteGyldigeOpptjeningAar: Int
)
