package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSpec

data class PoppOpptjeningsgrunnlagSpec(
    val fnr: String,
    val grunnlagstypeListe: List<PoppOpptjeningsgrunnlagType>?,
    val fomAr: Int?,
    val tomAr: Int?
) {
    companion object {
        fun fromInternalValue(source: OpptjeningsgrunnlagSpec) =
            PoppOpptjeningsgrunnlagSpec(
                fnr = source.pid.value,
                grunnlagstypeListe = source.grunnlagstypeListe.map(PoppOpptjeningsgrunnlagType::fromInternalValue)
                    .takeUnless { it.isEmpty() }, // "wildcard" is represented by null, not empty list
                fomAr = source.fomAar,
                tomAr = source.tomAar
            )
    }
}