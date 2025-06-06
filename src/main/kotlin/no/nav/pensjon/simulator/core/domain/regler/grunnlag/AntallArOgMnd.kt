package no.nav.pensjon.simulator.core.domain.regler.grunnlag

// 2025-06-06
class AntallArOgMnd {
    /**
     * Antall år som skal beskrives.
     */
    var antallAr = 0

    /**
     * Antall måneder som skal beskrives.
     */
    var antallMnd = 0

    constructor()
    constructor(antallAr: Int, antallMnd: Int) {
        this.antallAr = antallAr
        this.antallMnd = antallMnd
    }
}
