package no.nav.pensjon.simulator.tech.metric

import io.micrometer.core.instrument.Metrics

object Metrics {
    private const val PREFIX = "ps"

    fun countEgressCall(service: String, result: String) {
        Metrics
            .counter("$PREFIX-egress-call", "service", service, "result", result)
            .increment()
    }

    fun countEvent(eventName: String, result: String) {
        Metrics.counter("$PREFIX-$eventName", "result", result).increment()
    }
}

object MetricResult {
    const val BAD_CLIENT = "bad-client"
    const val BAD_SERVER = "bad-server"
    const val BAD_XML = "bad-xml"
    const val OK = "ok"
}
