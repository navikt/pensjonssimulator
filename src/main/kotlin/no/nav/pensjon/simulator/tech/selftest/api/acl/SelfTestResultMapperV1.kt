package no.nav.pensjon.simulator.tech.selftest.api.acl

import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.SelfTest.Companion.APPLICATION_NAME
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus
import java.time.LocalTime

object SelfTestResultMapperV1 {

    fun dto(pingResults: Map<String, PingResult>) =
        SelfTestResultV1(
            application = APPLICATION_NAME,
            timestamp = LocalTime.now().toString(),
            aggregateResult = deduceAggregateResult(pingResults.values).code,
            checks = pingResults.values.map(::dto)
        )

    private fun deduceAggregateResult(results: Collection<PingResult>) =
        results
            .map { it.status }
            .firstOrNull { it == ServiceStatus.DOWN } ?: ServiceStatus.UP

    private fun dto(result: PingResult) =
        CheckResultV1(
            endpoint = result.endpoint,
            description = result.service.description,
            errorMessage = if (result.status == ServiceStatus.DOWN) result.message else null,
            result = result.status.code
        )
}