package no.nav.pensjon

import mu.KotlinLogging
import no.nav.pensjon.client.ClientProvider.client
import no.nav.pensjon.Evaluator.evaluateResponseAtPath
import no.nav.pensjon.domain.Resource
import kotlin.system.exitProcess

suspend fun main() {
    val log = KotlinLogging.logger {}

    //add your test resources here with json files in main/resources folder
    val resourcesToTest = listOf(
        Resource(
            path = "/api/v0/simuler-afp-etterfulgt-av-alderspensjon",
            requestResource = "afp-etterfulgt-av-alder-request.json",
            responseResource = "afp-etterfulgt-av-alder-response.json",
        )
    )

    val results = resourcesToTest.map { evaluateResponseAtPath(it) }.toList()
    client.close()

    log.info("Evaluation results: $results")

    val failedTests = results.filter { !it.responseIsAsExpected }.toList()
    if (failedTests.isNotEmpty()) {
        val pathsWithDiffs = failedTests.map { "[" + it.path + ", diffs: " + it.diffs + "]" }
        val errorMessage = "Test failures: ${failedTests.size}, $failedTests $pathsWithDiffs"
        log.error(errorMessage)
        System.err.println(errorMessage)
        throw RuntimeException(errorMessage)
    }

    log.info("The job has been completed. Tests run: ${results.size}, failures: ${failedTests.size}")
}
