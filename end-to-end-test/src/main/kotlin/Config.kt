package no.nav.pensjon

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

data class MaskinportenConfig(
    val tokenEndpointUrl: String,
    val clientId: String,
    val clientJwk: String,
    val scope: String,
    val issuer: String
)

data class PensjonssimulatorConfig(
    val url: String,
)

fun loadMaskinportenConfig(): MaskinportenConfig {
    val maskinportenConfig: Config = ConfigFactory.load().getConfig("maskinporten")

    return MaskinportenConfig(
        tokenEndpointUrl = maskinportenConfig.getString("token-endpoint-url"),
        clientId = maskinportenConfig.getString("client-id"),
        clientJwk = maskinportenConfig.getString("client-jwk"),
        scope = maskinportenConfig.getString("scope"),
        issuer = maskinportenConfig.getString("issuer")
    )
}

fun loadPensjonssimulatorConfig(): PensjonssimulatorConfig {
    return PensjonssimulatorConfig(
        url = ConfigFactory.load().getString("pensjonssimulator.url")
    )
}

fun loadSlackUrl(): String {
    return ConfigFactory.load().getString("slack.webhook-url")
}