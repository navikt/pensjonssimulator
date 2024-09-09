package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable

/**
 * @author Aasmund Nordstoga (Accenture) PK-5549
 */
class Overgangsbelop : Serializable {
    var formelKode: FormelKodeCti? = null
    var gpBrutto: Int = 0
    var gpSats: Double = 0.0
    var overgangsbelop: Double = 0.0
    var stBrutto: Int = 0
    var tpBrutto: Int = 0

    constructor() : super() {}

    constructor(overgangsbelop: Overgangsbelop) {
        if (overgangsbelop.formelKode != null) {
            this.formelKode = FormelKodeCti(overgangsbelop.formelKode!!)
        }
        this.gpBrutto = overgangsbelop.gpBrutto
        this.gpSats = overgangsbelop.gpSats
        this.overgangsbelop = overgangsbelop.overgangsbelop
        this.stBrutto = overgangsbelop.stBrutto
        this.tpBrutto = overgangsbelop.tpBrutto
    }

    constructor(
            formelKode: FormelKodeCti? = null,
            gpBrutto: Int = 0,
            gpSats: Double = 0.0,
            overgangsbelop: Double = 0.0,
            stBrutto: Int = 0,
            tpBrutto: Int = 0
    ) {
        this.formelKode = formelKode
        this.gpBrutto = gpBrutto
        this.gpSats = gpSats
        this.overgangsbelop = overgangsbelop
        this.stBrutto = stBrutto
        this.tpBrutto = tpBrutto
    }

}
