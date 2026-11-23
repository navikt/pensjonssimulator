package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpOutput
import no.nav.pensjon.simulator.core.beregn.AlderspensjonBeregnerResult
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.spec.SimuleringSpec

data class ResultPreparerSpec(
    val simuleringSpec: SimuleringSpec,
    val kravhode: Kravhode,
    val alderspensjonsberegningsresultat: AlderspensjonBeregnerResult,
    val privatAfpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat>,
    val forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
    val forrigePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat?,
    val tidsbegrensetOffentligAfpBeregningResultat: Simuleringsresultat?,
    val livsvarigOffentligAfpBeregningResultatListe: List<LivsvarigOffentligAfpOutput>?,
    val outputSimulertBeregningsInformasjonForAllKnekkpunkter: Boolean,
    val registerData: RegisterData
)