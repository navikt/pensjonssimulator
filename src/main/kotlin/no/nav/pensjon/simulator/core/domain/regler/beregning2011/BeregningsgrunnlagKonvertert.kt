package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable
/**
 * @author Aasmund Nordstoga (Accenture) PK-5549
 */
class BeregningsgrunnlagKonvertert : AbstraktBeregningsgrunnlag, Serializable {

    var skattekompensertbelop: Skattekompensertbelop? = null

    var inntektVedSkadetidspunkt: Int = 0

    constructor() : super()

    constructor(beregningsgrunnlagKonvertert: BeregningsgrunnlagKonvertert) : super(beregningsgrunnlagKonvertert) {
        if (beregningsgrunnlagKonvertert.skattekompensertbelop != null) {
            this.skattekompensertbelop = Skattekompensertbelop(beregningsgrunnlagKonvertert.skattekompensertbelop!!)
        }
        this.inntektVedSkadetidspunkt = beregningsgrunnlagKonvertert.inntektVedSkadetidspunkt
    }

    constructor(
        skattekompensertbelop: Skattekompensertbelop? = null,
        inntektVedSkadetidspunkt: Int = 0,
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
        this.skattekompensertbelop = skattekompensertbelop
        this.inntektVedSkadetidspunkt = inntektVedSkadetidspunkt
    }
}
