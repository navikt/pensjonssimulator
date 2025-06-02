package no.nav.pensjon.reportfail

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import mu.KotlinLogging
import no.nav.pensjon.client.ClientProvider.client
import no.nav.pensjon.domain.EvaluationResult
import no.nav.pensjon.loadSlackUrl

object SlackReporter {
    val log = KotlinLogging.logger {}
    private val slackUrl = loadSlackUrl()

    suspend fun reportFailures(results: List<EvaluationResult>) {
        val totalTests = results.size
        val testFailures = results.count { !it.responseIsAsExpected }
        val pathsFailed = results.filter { !it.responseIsAsExpected }.map { it.path }
        val detectedChanges = results.filter { !it.responseIsAsExpected }.mapNotNull { it.diffs }

        val payload = """
            {
                "total_tests": $totalTests,
                "test_failures": $testFailures,
                "path_failed": ${pathsFailed.toJsonArray()},
                "detected_changes": ${detectedChanges.distinct().toJsonArray()}
            }
        """.trimIndent()

        log.info { "Reporting test failures to Slack" }
        client.post(slackUrl) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    private fun List<String>.toJsonArray(): String {
        return this.joinToString(prefix = "[", postfix = "]") { "\"${it.replace("\"", "\\\"")}\"" }
    }

}