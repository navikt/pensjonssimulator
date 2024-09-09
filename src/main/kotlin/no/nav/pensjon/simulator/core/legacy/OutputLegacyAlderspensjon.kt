package no.nav.pensjon.simulator.core.legacy

import no.nav.pensjon.simulator.core.result.PensjonPeriode

/**
 * Resultat av simulering av alderspensjon.
 * Dette objektet består av en liste som innholder pensjonsperioder med simulert utbetaling for hvert år,
 * og hvor stor del av utbetalingen som er basert på henholdsvis kapittel 19 og kapittel 20 regelverk.
 */
// no.nav.domain.pensjon.kjerne.simulering.SimulertAlderspensjon
data class OutputLegacyAlderspensjon(
    /**
     * Liste med pensjonsperioder som beskriver brukers simulerte pensjonsutbetaling for hver alder.
     */
    val pensjonPeriodeListe: List<PensjonPeriode> = mutableListOf(),

    /**
     * Andel av pensjonen som er beregnet etter kapittel 20
     */
    val kapittel20Andel: Double? = null,

    /**
     * Andel av pensjonen som er beregnet etter kapittel 19
     */
    val kapittel19Andel: Double? = null,

    val uttakGradListe: List<OutputLegacyUttakGrad> = mutableListOf(),
    val pensjonBeholdningListe: List<OutputLegacyPensjonBeholdningPeriode> = mutableListOf(),
    val simulertBeregningInformasjonListe: List<OutputLegacyBeregningInformasjon> = mutableListOf()
)
