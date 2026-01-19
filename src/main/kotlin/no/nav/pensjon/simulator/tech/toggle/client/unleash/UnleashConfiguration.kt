package no.nav.pensjon.simulator.tech.toggle.client.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UnleashConfiguration(
    @param:Value($$"${unleash.server.api.url}") private val endpoint: String,
    @param:Value($$"${unleash.server.api.token}") private val apiKey: String,
    @param:Value($$"${unleash.toggle.interval}") private val toggleInterval: String,
    @param:Value($$"${nais.cluster.name}") private val clusterName: String
) {
    @Bean
    open fun unleashConfig(@Value($$"${nais.app.name}") appName: String) =
        UnleashConfig.builder()
            .appName(appName)
            .environment(environment())
            .instanceId(instanceId())
            .fetchTogglesInterval(toggleInterval.toLong())
            .unleashAPI("$endpoint/api")
            .apiKey(apiKey)
            .build()

    @Bean
    open fun defaultUnleash(config: UnleashConfig) =
        DefaultUnleash(config)

    private fun environment() =
        if (clusterName == PRODUCTION_CLUSTER_NAME)
            PRODUCTION_ENVIRONMENT_NAME
        else
            DEFAULT_ENVIRONMENT_NAME

    private fun instanceId() = System.getProperty("instance.id") ?: DEFAULT_INSTANCE_ID

    companion object {
        const val PRODUCTION_CLUSTER_NAME = "prod-gcp"
        private const val PRODUCTION_ENVIRONMENT_NAME = "p"
        private const val DEFAULT_ENVIRONMENT_NAME = "q2"
        private const val DEFAULT_INSTANCE_ID = "local"
    }
}
