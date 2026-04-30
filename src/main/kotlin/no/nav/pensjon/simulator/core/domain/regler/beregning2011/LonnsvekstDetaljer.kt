package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import java.time.LocalDate

// 2026-04-23
class LonnsvekstDetaljer : ILonnsvekst {
    override var justeringTomDatoLd: LocalDate? = null
    override var justeringsfaktor = 0.0
    override var lonnsvekst = 0.0

    constructor()
}
