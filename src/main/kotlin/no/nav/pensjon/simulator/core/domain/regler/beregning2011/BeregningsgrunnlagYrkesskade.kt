package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti

/**
 * @author Steinar Hjellvik (Decisive) - PK-11391
 */
class BeregningsgrunnlagYrkesskade : AbstraktBeregningsgrunnlag {

    var inntektVedSkadetidspunkt: Int = 0

    /**
     * Angir det sluttpoengtall som yrkesskade beregningsgrunnlaget er omregnet fra.
     * Angår kun beregning av avdøde i sammenheng med nytt UT_GJT.
     */
    var sluttpoengtall: Double = 0.0

    constructor() : super()

    constructor(beregningsgrunnlagYrkesskade: BeregningsgrunnlagYrkesskade) : super(beregningsgrunnlagYrkesskade) {
        this.inntektVedSkadetidspunkt = beregningsgrunnlagYrkesskade.inntektVedSkadetidspunkt
        this.sluttpoengtall = beregningsgrunnlagYrkesskade.sluttpoengtall
    }

    constructor(
        inntektVedSkadetidspunkt: Int = 0,
        sluttpoengtall: Double = 0.0,
        /** super AbstraktBeregningsgrunnlag */
            formelKode: FormelKodeCti? = null,
        arsbelop: Int = 0,
        antattInntektFaktorKap19: Double = 0.0,
        antattInntektFaktorKap20: Double = 0.0
    ) : super(
            formelKode = formelKode,
            arsbelop = arsbelop,
            antattInntektFaktorKap19 = antattInntektFaktorKap19,
            antattInntektFaktorKap20 = antattInntektFaktorKap20
    ) {
        this.inntektVedSkadetidspunkt = inntektVedSkadetidspunkt
        this.sluttpoengtall = sluttpoengtall
    }
}
