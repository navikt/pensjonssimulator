package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.OmsorgTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag
import no.nav.pensjon.simulator.person.Pid

data class PoppOmsorg(
    val fnrOmsorgFor: String? = null,
    val omsorgType: String? = null,
    val ar: Int? = null
) {
    fun toGrunnlag() =
        Omsorgsgrunnlag().also {
            it.omsorgTypeEnum = type()
            it.personOmsorgFor = null // tilordnet senere
            it.bruk = true
            it.ar = ar ?: 0
            it.pidOmsorgFor = fnrOmsorgFor?.let(::Pid)
        }

    private fun type(): OmsorgTypeEnum? =
        omsorgType?.let { enumValueOf<OmsorgTypeEnum>(it) }
}