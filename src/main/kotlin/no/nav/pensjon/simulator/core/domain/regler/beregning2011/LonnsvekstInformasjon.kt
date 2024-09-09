package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable
import java.util.Date

class LonnsvekstInformasjon : Serializable {

    var lonnsvekst: Double = 0.0
    var reguleringsDato: Date? = null
    var uttaksgradVedRegulering: Int = 0

    constructor()

    constructor(li: LonnsvekstInformasjon) : this() {
        lonnsvekst = li.lonnsvekst
        if (li.reguleringsDato != null) {
            reguleringsDato = li.reguleringsDato
        }
        uttaksgradVedRegulering = li.uttaksgradVedRegulering
    }

    constructor(
            lonnsvekst: Double = 0.0,
            reguleringsDato: Date? = null,
            uttaksgradVedRegulering: Int = 0) {
        this.lonnsvekst = lonnsvekst
        this.reguleringsDato = reguleringsDato
        this.uttaksgradVedRegulering = uttaksgradVedRegulering
    }

}
