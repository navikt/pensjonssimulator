package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad

// no.nav.domain.pensjon.kjerne.simulering.SimulertAlderspensjon
class SimulertAlderspensjon {
    //TODO data class
    var simulertBeregningInformasjonListe: List<SimulertBeregningInformasjon> = emptyList()
    var pensjonBeholdningListe: List<BeholdningPeriode> = emptyList()
    val pensjonPeriodeListe: MutableList<PensjonPeriode> = mutableListOf()
    var uttakGradListe: List<Uttaksgrad> = emptyList()
    var kapittel19Andel: Double = 0.0
    var kapittel20Andel: Double = 0.0

    fun addPensjonsperiode(periode: PensjonPeriode?) {
        periode?.let(pensjonPeriodeListe::add)
    }
}
