package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-06-06
class BeregningsgrunnlagKonvertert : AbstraktBeregningsgrunnlag() {
    var skattekompensertbelop: Skattekompensertbelop? = null
    var inntektVedSkadetidspunkt = 0
}
