package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.util.*

class GReguleringDetaljer : IGRegulering {
    override var justeringTomDato: Date? = null
    override var justeringsfaktor: Double = 0.0
    override var forrigeG: Int = 0
    override var gjeldendeG: Int = 0

    constructor(greg: GReguleringDetaljer) : this() {
        if (greg.justeringTomDato != null) {
            justeringTomDato = greg.justeringTomDato!!.clone() as Date
        }
        justeringsfaktor = greg.justeringsfaktor
        forrigeG = greg.forrigeG
        gjeldendeG = greg.gjeldendeG
    }

    constructor(
            justeringTomDato: Date? = null,
            justeringsfaktor: Double = 0.0,
            forrigeG: Int = 0,
            gjeldendeG: Int = 0
    ) {
        this.justeringTomDato = justeringTomDato
        this.justeringsfaktor = justeringsfaktor
        this.forrigeG = forrigeG
        this.gjeldendeG = gjeldendeG
    }

}
