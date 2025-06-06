package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-06-06
class UtbetalingsgradUT {
    /**
     * året utbetalingsgraden gjelder for.
     */
    @JvmField
    var ar = 0

    /**
     * Utbetalingsgraden hentes fra uforetrygdOrdiner.avkortingsInformasjon.utbetalingsgrad.
     */
    var utbetalingsgrad = 0
}
