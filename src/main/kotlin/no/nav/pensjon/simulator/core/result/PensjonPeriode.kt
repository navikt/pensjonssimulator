package no.nav.pensjon.simulator.core.result

// no.nav.domain.pensjon.kjerne.simulering.Pensjonsperiode
class PensjonPeriode {
    //TODO data class
    /**
     * Totalt utbetalt beløp i perioden.
     */
    var beloep: Int? = null

    var maanedsbeloepVedPeriodeStart: Int? = null

    /**
     * Brukers alder i perioden
     */
    var alderAar: Int? = null

    /**
     * Beregningsinformasjon for en beregning gjort i pensjonsperioden.
     * Vil finnes dersom der har skjedd en uttaksgradsendring og/eller bruker blir 67 år i løpet av perioden.
     */
    var simulertBeregningInformasjonListe: MutableList<SimulertBeregningInformasjon> = mutableListOf()
}
