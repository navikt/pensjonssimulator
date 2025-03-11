package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.util.*

// 2025-03-11
class FremskrivingsDetaljer : IFremskriving {
    override var justeringTomDato: Date? = null
    override var justeringsfaktor = 0.0
    override var teller = 0.0
    override var nevner = 0.0
    var arskull = 0

    constructor()
}
