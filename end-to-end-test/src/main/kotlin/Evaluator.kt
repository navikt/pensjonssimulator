package no.nav.pensjon

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import no.nav.pensjon.client.ClientProvider.client
import no.nav.pensjon.client.MaskinportenToken.hentToken
import no.nav.pensjon.client.ResponseFormatter.format
import no.nav.pensjon.domain.Resource
import no.nav.pensjon.domain.EvaluationResult
import org.slf4j.LoggerFactory

object Evaluator {
    val log = LoggerFactory.getLogger(this::class.java)
    val pensjonssimulatorConfig = loadPensjonssimulatorConfig()

    suspend fun evaluateResponseAtPath(resource: Resource): EvaluationResult {

        try {
            val requestJson = readResourceAsText(resource.requestResource)
            val token = hentToken()

            val response: HttpResponse = client.post("${pensjonssimulatorConfig.url}${resource.path}") {
                headers { append(HttpHeaders.Authorization, token) }
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }

            if (response.status == HttpStatusCode.OK) {
                val expected = format(readResourceAsText(resource.responseResource))
                val actual = format(response.bodyAsText())
                val result = actual == expected

                log.info("Actual response is equal to expected: $result, actual response:$actual")
                return EvaluationResult(
                    responseIsAsExpected = result,
                    path = resource.path,
                    expectedResponsePath = resource.responseResource,
                    actualResponse = actual,
                )
            } else {
                val actualResponse = "response status: ${response.status}, response body: ${response.bodyAsText()}"
                log.error("HTTP Error from ${resource.path}: $actualResponse")
                return EvaluationResult(
                    responseIsAsExpected = false,
                    path = resource.path,
                    expectedResponsePath = resource.responseResource,
                    actualResponse = actualResponse,
                )
            }
        } catch (e: Exception) {
            val actualResponse = "Exception at ${resource.path}: ${e.message}"
            log.error(actualResponse, e)
            return EvaluationResult(
                responseIsAsExpected = false,
                path = resource.path,
                expectedResponsePath = resource.responseResource,
                actualResponse = actualResponse,
            )
        }
    }

    fun readResourceAsText(path: String): String {
        val stream = object {}.javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }
}