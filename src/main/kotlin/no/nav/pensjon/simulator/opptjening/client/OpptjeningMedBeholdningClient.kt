package no.nav.pensjon.simulator.opptjening.client

import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningSpec
import no.nav.pensjon.simulator.opptjening.Pensjonsopptjening

interface OpptjeningMedBeholdningClient {

    fun fetchOpptjening(spec: OpptjeningMedBeholdningSpec): Pensjonsopptjening
}