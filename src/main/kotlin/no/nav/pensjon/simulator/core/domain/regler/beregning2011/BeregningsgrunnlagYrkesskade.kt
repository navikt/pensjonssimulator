package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-03-10
class BeregningsgrunnlagYrkesskade : AbstraktBeregningsgrunnlag {
    var inntektVedSkadetidspunkt = 0

    /**
     * Angir det sluttpoengtall som yrkesskade beregningsgrunnlaget er omregnet fra.
     * Angår kun beregning av avdøde i sammenheng med nytt UT_GJT.
     */
    var sluttpoengtall = 0.0

    constructor() : super()

    constructor(source: BeregningsgrunnlagYrkesskade) : super(source) {
        this.inntektVedSkadetidspunkt = source.inntektVedSkadetidspunkt
        this.sluttpoengtall = source.sluttpoengtall
    }
}
