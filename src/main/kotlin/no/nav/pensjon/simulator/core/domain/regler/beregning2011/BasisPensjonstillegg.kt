package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-03-23
open class BasisPensjonstillegg : Pensjonstillegg {
    constructor()
    constructor(source: Pensjonstillegg) : super(source) {
        // Fjerner brutto og netto
        brutto = 0
        netto = 0
    }
}
