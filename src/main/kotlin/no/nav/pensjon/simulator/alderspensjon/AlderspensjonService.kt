package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.client.AlderspensjonClient
import org.springframework.stereotype.Component

@Component

class AlderspensjonService(private val client: AlderspensjonClient) {

    fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult =
        client.simulerAlderspensjon(spec.sanitise())
}
