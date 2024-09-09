package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class InntektsberegningsResultat {

    /**
     * Ny forventet inntekt for TSB.
     */

    var inntektTSB: InntektBT? = null

    /**
     * Ny forventet inntekt for TFB.
     */

    var inntektTFB: InntektBT? = null

    constructor() {}

    constructor(inntektsberegningsResultat: InntektsberegningsResultat) {
        if (inntektsberegningsResultat.inntektTFB != null) {
            inntektTFB = InntektBT(inntektsberegningsResultat.inntektTFB!!)
        }

        if (inntektsberegningsResultat.inntektTSB != null) {
            inntektTSB = InntektBT(inntektsberegningsResultat.inntektTSB!!)
        }
    }

    constructor(
            inntektTSB: InntektBT? = null,
            inntektTFB: InntektBT? = null) {
        this.inntektTSB = inntektTSB
        this.inntektTFB = inntektTFB
    }

}
