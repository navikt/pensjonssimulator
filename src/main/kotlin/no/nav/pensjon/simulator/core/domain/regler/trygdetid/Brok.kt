package no.nav.pensjon.simulator.core.domain.regler.trygdetid

import java.io.Serializable

/**
 * Brøk som brukes til
 * - (prorata) beregning av pensjon
 * - antall måneder barnetillegg
 */
class Brok : Serializable {
    /**
     * Brøkens teller.
     */
    var teller = 0

    /**
     * Brøkens nevner.
     */
    var nevner = 0

    constructor() : super()
    constructor(brok: Brok) : super() {
        teller = brok.teller
        nevner = brok.nevner
    }

    constructor(teller: Int = 0, nevner: Int = 0) : super() {
        this.teller = teller
        this.nevner = nevner
    }
}
