package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.util.*

// 2025-03-11
class LonnsvekstDetaljer : ILonnsvekst {
    override var justeringTomDato: Date? = null
    override var justeringsfaktor = 0.0
    override var lonnsvekst = 0.0

    constructor()
}
