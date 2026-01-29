package no.nav.pensjon.simulator.person.relasjon.client

import no.nav.pensjon.simulator.person.relasjon.PersonRelasjonStatus
import no.nav.pensjon.simulator.person.relasjon.PersonPar

interface PersonRelasjonClient {
    fun fetchPersonRelasjonStatus(personer: PersonPar): PersonRelasjonStatus?
}
