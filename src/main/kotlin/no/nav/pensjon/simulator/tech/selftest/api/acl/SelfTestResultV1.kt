package no.nav.pensjon.simulator.tech.selftest.api.acl

import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.SelfTest.Companion.APPLICATION_NAME
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus
import java.time.LocalTime

data class SelfTestResultV1(
    val application: String,
    val timestamp: String,
    val aggregateResult: Int,
    val checks: List<CheckResultV1>
) {
    companion object {
        /**
         * Maps from domain representation to transferable format for the API.
         */
        fun transferable(resultsByService: Map<String, PingResult>, time: LocalTime) =
            SelfTestResultV1(
                application = APPLICATION_NAME,
                timestamp = time.toString(),
                aggregateResult = deduceAggregateResult(resultsByService.values).code,
                checks = resultsByService.values.map(CheckResultV1::transferable)
            )

        private fun deduceAggregateResult(results: Collection<PingResult>) =
            results
                .map { it.status }
                .firstOrNull { it == ServiceStatus.DOWN } ?: ServiceStatus.UP
    }
}