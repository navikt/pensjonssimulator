package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon

class BasisTilleggspensjon : Tilleggspensjon {

    constructor()

    constructor(source: Tilleggspensjon) : super(source) {
        // Fjerner brutto og netto
        brutto = 0
        netto = 0
    }
}
