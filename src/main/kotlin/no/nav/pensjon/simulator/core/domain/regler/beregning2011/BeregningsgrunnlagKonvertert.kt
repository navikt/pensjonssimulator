package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-03-10
class BeregningsgrunnlagKonvertert : AbstraktBeregningsgrunnlag {
    var skattekompensertbelop: Skattekompensertbelop? = null
    var inntektVedSkadetidspunkt = 0

    constructor() : super()

    constructor(source: BeregningsgrunnlagKonvertert) : super(source) {
        skattekompensertbelop = source.skattekompensertbelop?.let(::Skattekompensertbelop)
        inntektVedSkadetidspunkt = source.inntektVedSkadetidspunkt
    }
}
