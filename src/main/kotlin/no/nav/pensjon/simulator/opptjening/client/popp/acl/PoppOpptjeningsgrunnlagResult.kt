package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSamling

data class PoppOpptjeningsgrunnlagResult(
    val opptjeningsGrunnlag: PoppOpptjeningsgrunnlag? = null
) {
    fun toInternalValue(): OpptjeningsgrunnlagSamling? =
        opptjeningsGrunnlag?.toInternalValue()
}