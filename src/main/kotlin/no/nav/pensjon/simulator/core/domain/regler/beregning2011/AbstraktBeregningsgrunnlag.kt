package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable

/**
 * @author Steinar Hjellvik (Decisive) - PK-6458
 * @author Magnus Bakken (Accenture) - PK-20759
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = BeregningsgrunnlagKonvertert::class),
    JsonSubTypes.Type(value = BeregningsgrunnlagOrdiner::class),
    JsonSubTypes.Type(value = BeregningsgrunnlagYrkesskade::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBeregningsgrunnlag : Serializable {

    var formelKode: FormelKodeCti? = null

    var arsbelop: Int = 0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 19).
     */
    var antattInntektFaktorKap19: Double = 0.0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 20).
     */
    var antattInntektFaktorKap20: Double = 0.0

    protected constructor() : super() {}

    protected constructor(abstraktBeregningsgrunnlag: AbstraktBeregningsgrunnlag) {
        if (abstraktBeregningsgrunnlag.formelKode != null) {
            formelKode = FormelKodeCti(abstraktBeregningsgrunnlag.formelKode!!)
        }
        arsbelop = abstraktBeregningsgrunnlag.arsbelop
        antattInntektFaktorKap19 = abstraktBeregningsgrunnlag.antattInntektFaktorKap19
        antattInntektFaktorKap20 = abstraktBeregningsgrunnlag.antattInntektFaktorKap20
    }

    constructor(
            formelKode: FormelKodeCti? = null,
            arsbelop: Int = 0,
            antattInntektFaktorKap19: Double = 0.0,
            antattInntektFaktorKap20: Double = 0.0
    ) {
        this.formelKode = formelKode
        this.arsbelop = arsbelop
        this.antattInntektFaktorKap19 = antattInntektFaktorKap19
        this.antattInntektFaktorKap20 = antattInntektFaktorKap20
    }
}
