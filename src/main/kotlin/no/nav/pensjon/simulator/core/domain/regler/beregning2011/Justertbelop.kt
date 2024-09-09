package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable

/**
 * @author Aasmund Nordstoga (Accenture) PK-5549
 */
class Justertbelop : Serializable {
    var g01052014: Int = 0
    var gjennomsnittligG2014: Int = 0
    var justertbelop: Double = 0.0
    var overgangsbelop: Overgangsbelop? = null

    constructor() : super() {}

    constructor(justertbelop: Justertbelop) {
        this.g01052014 = justertbelop.g01052014
        this.gjennomsnittligG2014 = justertbelop.gjennomsnittligG2014
        this.justertbelop = justertbelop.justertbelop
        if (justertbelop.overgangsbelop != null) {
            this.overgangsbelop = Overgangsbelop(justertbelop.overgangsbelop!!)
        }
    }

    constructor(
            g01052014: Int = 0,
            gjennomsnittligG2014: Int = 0,
            justertbelop: Double = 0.0,
            overgangsbelop: Overgangsbelop? = null
    ) {
        this.g01052014 = g01052014
        this.gjennomsnittligG2014 = gjennomsnittligG2014
        this.justertbelop = justertbelop
        this.overgangsbelop = overgangsbelop
    }

}
