package no.nav.pensjon.simulator.core.domain.regler.trygdetid

// 2025-06-06
/**
 * brøk som brukes til
 * - (prorata) beregning av pensjon
 * - antall måneder barnetillegg
 */
class Brok {
    /**
     * brøkens teller.
     */
    var teller = 0

    /**
     * brøkens nevner.
     */
    var nevner = 0

    constructor()
    constructor(brok: Brok) : super() {
        teller = brok.teller
        nevner = brok.nevner
    }
}
