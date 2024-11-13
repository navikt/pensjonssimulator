package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import org.springframework.stereotype.Component

@Component
class BeholdningerMedGrunnlagService(private val client: BeholdningClient) {

    fun getBeholdningerMedGrunnlag(spec: BeholdningerMedGrunnlagSpec): BeholdningerMedGrunnlagResult =
        client.fetchBeholdningerMedGrunnlag(spec)
}
