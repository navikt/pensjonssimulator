package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

data class PoppBeholdningResult(
    val beholdninger: MutableList<PoppBeholdning>? = null
) {
    fun toInternalValue(): List<Pensjonsbeholdning> =
        beholdninger.orEmpty().map { it.toPensjonsbeholdning() }
}