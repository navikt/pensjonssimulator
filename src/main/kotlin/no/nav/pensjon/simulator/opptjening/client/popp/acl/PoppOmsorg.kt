package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag
import no.nav.pensjon.simulator.person.Pid

data class PoppOmsorg(
    val fnrOmsorgFor: String? = null,
    val omsorgType: String? = null, // OmsorgTypeEnum
    val ar: Int? = null
) {
    fun toGrunnlag() =
        Omsorgsgrunnlag().also {
            it.ar = ar ?: 0
            it.omsorgTypeEnum = omsorgType?.let(::enumValueOf)
            it.pidOmsorgFor = fnrOmsorgFor?.let(::Pid)
            it.personOmsorgFor = null // tilordnet senere
            it.bruk = true
        }
}