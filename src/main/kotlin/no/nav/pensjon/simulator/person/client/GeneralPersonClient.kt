package no.nav.pensjon.simulator.person.client

import no.nav.pensjon.simulator.person.Person
import no.nav.pensjon.simulator.person.Pid

interface GeneralPersonClient {
    fun fetchPerson(pid: Pid): Person?
}
