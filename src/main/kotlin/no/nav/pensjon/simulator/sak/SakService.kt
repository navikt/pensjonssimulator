package no.nav.pensjon.simulator.sak

import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import no.nav.pensjon.simulator.sak.client.SakClient
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service

@Service
class SakService(private val client: SakClient) {

    fun personVirkningDato(pid: Pid): FoersteVirkningDatoCombo = client.fetchPersonVirkningDato(pid)
}
