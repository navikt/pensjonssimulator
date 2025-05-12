package no.nav.pensjon.simulator.tech.toggle.client

import org.springframework.stereotype.Service

@Service
interface FeatureToggleClient {
    fun isEnabled(featureName: String): Boolean
}
