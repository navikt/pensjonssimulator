package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-06-06
class BeregningsgrunnlagYrkesskade : AbstraktBeregningsgrunnlag() {
    var inntektVedSkadetidspunkt = 0

    /**
     * Angir det sluttpoengtall som yrkesskade beregningsgrunnlaget er omregnet fra.
     * Angår kun beregning av avdøde i sammenheng med nytt UT_GJT.
     */
    var sluttpoengtall = 0.0
}
