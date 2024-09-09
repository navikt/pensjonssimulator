package no.nav.pensjon.simulator.core.domain.regler.beregning

import java.io.Serializable

/**
 * @author Magnus Bakken (Accenture) PK-9158
 */
class KonverteringsdataUT : Serializable {

    /**
     * Verdien av tp_up
     */
    var tpUfor: Int = 0

    /**
     * Verdien av tp_yp
     */
    var tpYrke: Int = 0

    constructor() {
        tpUfor = 0
        tpYrke = 0
    }

    /**
     * Copy Constructor
     *
     * @param konverteringsdata a `KonverteringsdataUT` object
     */
    constructor(konverteringsdata: KonverteringsdataUT) {
        tpUfor = konverteringsdata.tpUfor
        tpYrke = konverteringsdata.tpYrke
    }

    constructor(tpUfor: Int = 0, tpYrke: Int = 0) {
        this.tpUfor = tpUfor
        this.tpYrke = tpYrke
    }
}
