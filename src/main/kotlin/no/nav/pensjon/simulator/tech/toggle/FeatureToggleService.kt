package no.nav.pensjon.simulator.tech.toggle

import no.nav.pensjon.simulator.tech.toggle.client.FeatureToggleClient
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(val client: FeatureToggleClient) {
    fun isEnabled(featureName: String) = client.isEnabled(featureName)
}
