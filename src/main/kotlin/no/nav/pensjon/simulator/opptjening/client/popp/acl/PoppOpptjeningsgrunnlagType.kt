package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagType

enum class PoppOpptjeningsgrunnlagType(val internalValue: OpptjeningsgrunnlagType) {

    DAGPENGER(internalValue = OpptjeningsgrunnlagType.DAGPENGER),
    FORST_TJENESTE(internalValue = OpptjeningsgrunnlagType.FOERSTEGANGSTJENESTE),
    INNTEKT(internalValue = OpptjeningsgrunnlagType.INNTEKT),
    OMSORG(internalValue = OpptjeningsgrunnlagType.OMSORG);

    companion object {
        private val valuesByInternal = entries.associateBy { it.internalValue }

        fun fromInternalValue(value: OpptjeningsgrunnlagType): PoppOpptjeningsgrunnlagType =
            valuesByInternal[value]
                ?: throw IllegalArgumentException("internal value $value not supported")
    }
}