package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.time.LocalDate

// 2026-04-23
class FremskrivingsDetaljer : IFremskriving {
    override var justeringTomDatoLd: LocalDate? = null
    override var justeringsfaktor = 0.0
    override var teller = 0.0
    override var nevner = 0.0
    var arskull = 0

    constructor()
}
