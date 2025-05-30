package no.nav.pensjon

import mu.KotlinLogging
import no.nav.pensjon.client.ClientProvider.client
import no.nav.pensjon.Evaluator.evaluateResponseAtPath
import no.nav.pensjon.domain.Resource

suspend fun main() {
    val log = KotlinLogging.logger {}

    //add your test resources here with json files in main/resources folder
    val resourcesToTest = listOf(
        Resource(
            path = "/api/v0/simuler-afp-etterfulgt-av-alderspensjon",
            requestResource = "afp-etterfulgt-av-alder-request.json",
            responseResource = "afp-etterfulgt-av-alder-response.json",
        ),
        Resource(
            path = "/api/v4/simuler-alderspensjon",
            requestResource = "simuler-alderspensjon-v4-request.json",
            responseResource = "simuler-alderspensjon-v4-response.json",
        )
    )

    val results = resourcesToTest.map { evaluateResponseAtPath(it) }.toList()

    log.info("Evaluation results: $results")

    val failedTests = results.filter { !it.responseIsAsExpected }.toList()
    if (failedTests.isNotEmpty()) {
        log.error("Test failures: ${failedTests.size}, $failedTests")
    }

    client.close()
    log.info("The job has been completed. Tests run: ${results.size}, failures: ${failedTests.size}")
}
