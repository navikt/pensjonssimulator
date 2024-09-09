package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.io.Serializable
import java.util.*

class FremskrivingsDetaljer : Serializable, IFremskriving {
    override var justeringTomDato: Date? = null
    override var justeringsfaktor: Double = 0.0
    override var teller: Double = 0.0
    override var nevner: Double = 0.0
    var arskull: Int = 0

    constructor()

    constructor(frem: FremskrivingsDetaljer) : this() {
        if (frem.justeringTomDato != null) {
            justeringTomDato = frem.justeringTomDato!!.clone() as Date
        }
        justeringsfaktor = frem.justeringsfaktor
        teller = frem.teller
        nevner = frem.nevner
        arskull = frem.arskull
    }

    constructor(
            justeringTomDato: Date? = null,
            justeringsfaktor: Double = 0.0,
            teller: Double = 0.0,
            nevner: Double = 0.0,
            arskull: Int = 0
    ) {
        this.justeringTomDato = justeringTomDato
        this.justeringsfaktor = justeringsfaktor
        this.teller = teller
        this.nevner = nevner
        this.arskull = arskull
    }
}
