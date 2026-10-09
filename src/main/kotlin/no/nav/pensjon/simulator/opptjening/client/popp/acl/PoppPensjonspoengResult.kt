package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag

data class PoppPensjonspoengResult(
    val pensjonspoeng: List<PoppPensjonspoeng> = emptyList()
) {
    fun toInternalValue(): List<Opptjeningsgrunnlag> =
        pensjonspoeng.map { it.toGrunnlag() }
}