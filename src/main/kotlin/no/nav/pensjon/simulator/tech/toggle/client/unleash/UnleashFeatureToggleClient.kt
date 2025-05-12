package no.nav.pensjon.simulator.tech.toggle.client.unleash

import io.getunleash.Unleash
import no.nav.pensjon.simulator.tech.toggle.client.FeatureToggleClient
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class UnleashFeatureToggleClient(private val unleash: Unleash) : FeatureToggleClient {
    override fun isEnabled(featureName: String) = unleash.isEnabled(featureName)
}
