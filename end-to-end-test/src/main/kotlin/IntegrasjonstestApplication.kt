package no.nav.pensjon

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

suspend fun main() {
    val log = LoggerFactory.getLogger("IntegrasjonstestApplication")
    val pensjonssimulatorConfig = loadPensjonssimulatorConfig()
    log.info("Loaded pensjonssimulator config")

    val requestJson = readResourceAsText("afp-etterfulgt-av-alder-request.json")
    val expectedJson = Json.parseToJsonElement(readResourceAsText("afp-etterfulgt-av-alder-response.json"))

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }
    val maskinportenTokenService = MaskinportenTokenService(client)
    log.info("Loaded maskinporten config")
    val token = maskinportenTokenService.hentToken()
    log.info("Hentet maskinporten-token:$token")

    try {
        val response = client.post("${pensjonssimulatorConfig.url}/api/v0/simuler-afp-etterfulgt-av-alderspensjon") {
            headers { append(HttpHeaders.Authorization, token) }
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        if (response.status == HttpStatusCode.OK) {
            val actualJson = Json.parseToJsonElement(response.bodyAsText())
            val result = if (actualJson == expectedJson) "MATCH" else "MISMATCH"
            log.info("Response comparison result: $result with actualJson:$actualJson")
        } else {
            log.error("Unexpected response status: ${response.status}")
            log.error("Response body: ${response.bodyAsText()}")
        }
    } catch (e: Exception) {
        log.error("Request failed: ${e.message}", e)
    } finally {
        client.close()
    }
    log.info("The job has been completed")
}

fun readResourceAsText(path: String): String {
    val stream = object {}.javaClass.classLoader.getResourceAsStream(path)
        ?: throw IllegalArgumentException("Resource not found: $path")
    return stream.bufferedReader().use { it.readText() }
}
