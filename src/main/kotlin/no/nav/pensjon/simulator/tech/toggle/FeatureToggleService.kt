package no.nav.pensjon.simulator.tech.toggle

import no.nav.pensjon.simulator.tech.toggle.client.FeatureToggleClient
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(val client: FeatureToggleClient) {
    fun isEnabled(featureName: String) = client.isEnabled(featureName)

    companion object {
        const val PEN_715_SIMULER_SPK = "tjenestepensjon-simulering.hent-oftp-fra-spk"
    }
}
