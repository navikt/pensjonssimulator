package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsordning
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent

interface StillingsprosentClient {

    fun getStillingsprosenter(pid: Pid, tpOrdning: Tjenestepensjonsordning): List<Stillingsprosent>
}