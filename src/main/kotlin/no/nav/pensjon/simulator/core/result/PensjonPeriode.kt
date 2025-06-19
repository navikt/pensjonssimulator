package no.nav.pensjon.simulator.core.result

// PEN: no.nav.domain.pensjon.kjerne.simulering.Pensjonsperiode
class PensjonPeriode {
    //TODO data class
    /**
     * Totalt utbetalt beløp i perioden.
     */
    var beloep: Int? = null

    var maanedsutbetalinger: List<Maanedsutbetaling> = mutableListOf()

    /**
     * Brukers alder i perioden
     */
    var alderAar: Int? = null

    /**
     * Beregningsinformasjon for en beregning gjort i pensjonsperioden.
     * Vil finnes dersom der har skjedd en uttaksgradsendring og/eller bruker blir 67 år i løpet av perioden.
     */
    var simulertBeregningInformasjonListe: MutableList<SimulertBeregningInformasjon> = mutableListOf()

    //--- Extra:
    val foerstePensjonsbeholdningFoerUttak: Int?
        get() = simulertBeregningInformasjonListe.firstOrNull { it.pensjonBeholdningFoerUttak != null }
            ?.pensjonBeholdningFoerUttak
}
