package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon

// 2025-03-23
class BasisTilleggspensjon : Tilleggspensjon {
    constructor()
    constructor(source: Tilleggspensjon) : super(source) {
        // Fjerner brutto og netto
        brutto = 0
        netto = 0
    }
}
