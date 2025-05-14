package no.nav.pensjon

import no.nav.pensjon.client.ClientProvider.client
import no.nav.pensjon.Evaluator.evaluateResponseAtPath
import no.nav.pensjon.domain.Resource
import org.slf4j.LoggerFactory

suspend fun main() {
    val log = LoggerFactory.getLogger("IntegrasjonstestApplication")

    //add your test resources here with json files in main/resources folder
    val resourcesToTest = listOf(
        Resource(
            path = "/api/v0/simuler-afp-etterfulgt-av-alderspensjon",
            requestResource = "afp-etterfulgt-av-alder-request.json",
            responseResource = "afp-etterfulgt-av-alder-response.json",
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
