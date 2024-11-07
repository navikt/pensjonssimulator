package no.nav.pensjon.simulator.person

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.client.PersonClient
import org.springframework.stereotype.Service

@Service
class PersonService(private val client: PersonClient) {

    fun personListe(pidListe: List<Pid>): Map<Pid, PenPerson> =
        client.fetchPersonerVedPid(pidListe)
}
