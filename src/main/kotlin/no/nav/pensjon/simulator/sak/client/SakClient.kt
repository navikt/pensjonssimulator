package no.nav.pensjon.simulator.sak.client

import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.person.Pid

interface SakClient {
    fun fetchPersonVirkningDato(pid: Pid): FoersteVirkningDatoCombo
}
