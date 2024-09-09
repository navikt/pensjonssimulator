package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.person.Pid

// no.nav.service.pensjon.beregning.OppdaterPensjonsbeholdningerResponse
class BeholdningUpdateResult(
    val personBeholdningListe: List<PersonBeholdning>,
    val fnrIkkeFunnetListe: List<Pid> //TODO delete?
)
