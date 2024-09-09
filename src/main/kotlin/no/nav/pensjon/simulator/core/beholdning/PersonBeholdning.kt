package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.person.Pid

/**
 * Wrapper-objekt som holder på en liste med Beholdningsobjekter som er blitt beregnet
 * samt et fnr som angir personen beholdningene tilhører.
 * Objektet brukes for å sende en liste av beregnede beholdninger til POPP.
 */
// no.nav.domain.pensjon.kjerne.grunnlag.PersonBeholdning
class PersonBeholdning {
    //TODO data class + val
    var pid: Pid? = null
    var beholdningListe: MutableList<Pensjonsbeholdning> = mutableListOf()
}
