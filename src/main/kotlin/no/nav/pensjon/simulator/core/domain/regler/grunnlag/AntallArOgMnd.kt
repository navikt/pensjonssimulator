package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class AntallArOgMnd(
    /**
     * Antall år som skal beskrives.
     */
    var antallAr: Int = 0,
    /**
     * Antall måneder som skal beskrives.
     */
    var antallMnd: Int = 0
) : Serializable {
    constructor(antallArOgMnd: AntallArOgMnd) : this() {
        this.antallAr = antallArOgMnd.antallAr
        this.antallMnd = antallArOgMnd.antallMnd
    }
}
