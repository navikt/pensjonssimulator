package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable

/**
 * @author Steinar Hjellvik (Decisive) - PK-6458
 */
class UtbetalingsgradUT : Serializable {
    /**
     * Året utbetalingsgraden gjelder for.
     */
    var ar: Int = 0

    /**
     * Utbetalingsgraden hentes fra uforetrygdOrdiner.avkortingsInformasjon.utbetalingsgrad.
     */
    var utbetalingsgrad: Int = 0

    constructor() : super() {}

    constructor(aUtbetalingsgradUT: UtbetalingsgradUT) {
        ar = aUtbetalingsgradUT.ar
        utbetalingsgrad = aUtbetalingsgradUT.utbetalingsgrad
    }

    constructor(
            ar: Int = 0,
            utbetalingsgrad: Int = 0
    ) {
        this.ar = ar
        this.utbetalingsgrad = utbetalingsgrad
    }

}
