package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

// 2025-03-10
@JsonSubTypes(
    JsonSubTypes.Type(value = BeregningsgrunnlagKonvertert::class),
    JsonSubTypes.Type(value = BeregningsgrunnlagOrdiner::class),
    JsonSubTypes.Type(value = BeregningsgrunnlagYrkesskade::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBeregningsgrunnlag {
    var formelKodeEnum: FormelKodeEnum? = null
    var arsbelop = 0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 19).
     */
    var antattInntektFaktorKap19 = 0.0

    /**
     * Antatt årlig inntekt før uføretidspunktet (brukes i fastsettelse av opptjening til alderspensjon etter kapittel 20).
     */
    var antattInntektFaktorKap20 = 0.0

    protected constructor() : super() {}

    protected constructor(source: AbstraktBeregningsgrunnlag) {
        formelKodeEnum = source.formelKodeEnum
        arsbelop = source.arsbelop
        antattInntektFaktorKap19 = source.antattInntektFaktorKap19
        antattInntektFaktorKap20 = source.antattInntektFaktorKap20
    }
}
