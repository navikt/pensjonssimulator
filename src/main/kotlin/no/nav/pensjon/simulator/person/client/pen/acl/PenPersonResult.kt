package no.nav.pensjon.simulator.person.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.Pid

data class PenPersonResult(
    val personerVedPid: Map<String, PenPerson>
){
    fun withPidAsKeys(): Map<Pid, PenPerson> =
        personerVedPid.mapKeys { Pid(it.key) }
}
