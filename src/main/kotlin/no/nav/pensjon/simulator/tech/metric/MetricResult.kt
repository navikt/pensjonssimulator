package no.nav.pensjon.simulator.tech.metric

import io.micrometer.core.instrument.Metrics

object Metrics {
    private const val PREFIX = "ps"

    fun countEgressCall(service: String, result: String) {
        Metrics
            .counter("$PREFIX-egress-call", "service", service, "result", result)
            .increment()
    }

    fun countIngressCall(organisasjonId: String, callId: String) {
        Metrics
            .counter("$PREFIX-ingress-call", "org", organisasjonId, "call", callId)
            .increment()
    }

    fun countSimuleringstype(type: String) {
        Metrics
            .counter("$PREFIX-simuleringstype", "type", type)
            .increment()
    }
}

object MetricResult {
    const val BAD_CLIENT = "bad-client"
    const val BAD_SERVER = "bad-server"
    const val OK = "ok"
}
