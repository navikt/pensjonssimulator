package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-03-10
class BeregningsgrunnlagOrdiner : AbstraktBeregningsgrunnlag {
    var opptjeningUTListe: List<OpptjeningUT> = mutableListOf()
    var nasjonaltSnitt = 0

    /**
     * Angir det sluttpoengtall som ordinårt beregningsgrunnlag er omregnet fra.
     * Angår kun beregning av avdøde i sammenheng med nytt UT_GJT.
     */
    var sluttpoengtall = 0.0

    constructor() : super() {
        opptjeningUTListe = mutableListOf()
    }

    constructor(source: BeregningsgrunnlagOrdiner) : super(source) {
        opptjeningUTListe = source.opptjeningUTListe.map(::OpptjeningUT)
        nasjonaltSnitt = source.nasjonaltSnitt
        sluttpoengtall = source.sluttpoengtall
    }
}
