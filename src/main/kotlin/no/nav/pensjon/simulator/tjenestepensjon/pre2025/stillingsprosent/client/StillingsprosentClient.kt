package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.TpOrdning

interface StillingsprosentClient {

    fun getStillingsprosenter(pid: Pid, tpOrdning: TpOrdning): List<Stillingsprosent>
}