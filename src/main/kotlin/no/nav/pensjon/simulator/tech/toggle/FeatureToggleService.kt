package no.nav.pensjon.simulator.tech.toggle

import no.nav.pensjon.simulator.tech.toggle.client.FeatureToggleClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(@Qualifier("featureToggleClient") val client: FeatureToggleClient) {
    fun isEnabled(featureName: String) = client.isEnabled(featureName)
}
