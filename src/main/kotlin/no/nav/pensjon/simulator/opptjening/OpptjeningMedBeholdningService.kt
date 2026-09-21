package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.opptjening.client.OpptjeningMedBeholdningClient
import org.springframework.stereotype.Component

@Component
class OpptjeningMedBeholdningService(private val client: OpptjeningMedBeholdningClient) {

    fun pensjonsopptjening(spec: OpptjeningMedBeholdningSpec): Pensjonsopptjening =
        client.fetchOpptjening(spec.medBeholdninger()).let {
            if (spec.hentBeholdninger) it else it.utenBeholdninger()
        }
}