package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti

/**
 * Wrapper classe som PEN trenger
 *
 * @author Torje Coldevin
 */
class TapendeBeregningsmetode {

    var beregningMetodeTypeCti: BeregningMetodeTypeCti? = null
    var tapendeBeregningsmetodeId: Long = 0

    constructor(beregningMetodeTypeCti: BeregningMetodeTypeCti) {
        this.beregningMetodeTypeCti = beregningMetodeTypeCti
    }

    constructor() {}

    constructor(tapendeBeregningsmetode: TapendeBeregningsmetode) {
        if (tapendeBeregningsmetode.beregningMetodeTypeCti != null) {
            beregningMetodeTypeCti = BeregningMetodeTypeCti(tapendeBeregningsmetode.beregningMetodeTypeCti)
        }
        tapendeBeregningsmetodeId = tapendeBeregningsmetode.tapendeBeregningsmetodeId
    }

    constructor(
            beregningMetodeTypeCti: BeregningMetodeTypeCti? = null,
            tapendeBeregningsmetodeId: Long = 0) {
        this.beregningMetodeTypeCti = beregningMetodeTypeCti
        this.tapendeBeregningsmetodeId = tapendeBeregningsmetodeId
    }

}
