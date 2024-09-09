package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning

class VilkarsprovAlderspensjon67Resultat : AbstraktVilkarsprovResultat {

    var beregningVedUttak: Beregning? = null

    var halvMinstePensjon: Int = 0

    constructor(beregningVedUttak: Beregning? = null, halvMinstePensjon: Int = 0) : super() {
        this.beregningVedUttak = beregningVedUttak
        this.halvMinstePensjon = halvMinstePensjon
    }

    constructor(r: VilkarsprovAlderspensjon67Resultat?) : super() {
        if (r != null) {
            this.beregningVedUttak = Beregning(r.beregningVedUttak)
            this.halvMinstePensjon = r.halvMinstePensjon
        }
    }

    // For jackson
    @Suppress("unused")
    constructor() : super()
}
