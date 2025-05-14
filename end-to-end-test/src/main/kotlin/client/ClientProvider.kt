package no.nav.pensjon.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingConfig
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ClientProvider {
    val client = HttpClient(CIO) {
        HttpClientConfig.install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        HttpClientConfig.install(Logging) {
            LoggingConfig.logger = Logger.Companion.DEFAULT
            LoggingConfig.level = LogLevel.ALL
        }
    }

}