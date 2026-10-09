package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSamling

data class PoppOpptjeningsgrunnlagResult(
    val opptjeningsGrunnlag: PoppOpptjeningsgrunnlag? = null
) {
    fun toInternalValue(): OpptjeningsgrunnlagSamling? =
        opptjeningsGrunnlag?.toInternalValue()

    fun toLoependeInntekt(): LoependeInntekt? =
        opptjeningsGrunnlag?.toLoependeInntekt()
}