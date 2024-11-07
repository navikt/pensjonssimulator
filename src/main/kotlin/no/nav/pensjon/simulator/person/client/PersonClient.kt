package no.nav.pensjon.simulator.person.client

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.Pid

interface PersonClient {
    fun fetchPersonerVedPid(pidListe: List<Pid>): Map<Pid, PenPerson>
}
