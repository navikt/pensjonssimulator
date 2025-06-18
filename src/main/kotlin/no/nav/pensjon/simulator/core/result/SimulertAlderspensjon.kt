package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import java.time.LocalDate

// PEN: no.nav.domain.pensjon.kjerne.simulering.SimulertAlderspensjon
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

    //--- Extra:
    /**
     * Finner garantipensjonsbeholdningen med f.o.m.-dato nærmest, men før, angitt dato.
     * Hvis ingen garantipensjonsbeholdning har f.o.m.-dato før angitt dato,
     * returneres den som har f.o.m.-dato nærmest, men etter, angitt dato.
     */
    fun garantipensjonsbeholdningVedDato(dato: LocalDate): Int? =
        beholdningVedDato(pensjonBeholdningListe, dato)?.garantipensjonsbeholdning?.toInt()

    private companion object {
        private fun beholdningVedDato(beholdningListe: List<BeholdningPeriode>, dato: LocalDate): BeholdningPeriode? =
            beholdningListe.filter { it.datoFom.isBefore(dato) }.maxByOrNull { it.datoFom }
                ?: beholdningListe.minByOrNull { it.datoFom }
    }
}
