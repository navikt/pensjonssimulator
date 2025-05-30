package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon

// 2025-03-20
class BasisGrunnpensjon : Grunnpensjon {
    /**
     * En versjon av Grunnpensjon uten tilgang til brutto og netto siden Basispensjonsytelsene kun
     * er definert med årsbeløp
     */
    constructor(source: Grunnpensjon) : super(source) {
        // Fjerner brutto og netto
        super.brutto = 0
        super.netto = 0
    }

    constructor()
}
