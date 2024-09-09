package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class AntallArMndDag(
    /**
     * Antall år som skal beskrives.
     */
    var antallAr: Int = 0,
    /**
     * Antall måneder som skal beskrives.
     */
    var antallMnd: Int = 0,
    /**
     * Antall dager som skal beskrives.
     */
    var antallDager: Int = 0
) : Serializable {
    constructor(antallArMndDag: AntallArMndDag) : this() {
        this.antallAr = antallArMndDag.antallAr
        this.antallMnd = antallArMndDag.antallMnd
        this.antallDager = antallArMndDag.antallDager
    }

}
