package no.nav.pensjon.simulator.tech.metric

import io.micrometer.core.instrument.Metrics

object Metrics {
    private const val PREFIX = "ps"

    fun countEgressCall(service: String, result: String) {
        Metrics
            .counter("$PREFIX-egress-call", "service", service, "result", result)
            .increment()
    }

    fun countIngressCall(organisasjonsnummer: String) {
        Metrics.counter("$PREFIX-ingress-call", "orgno", organisasjonsnummer).increment()
    }
}

object MetricResult {
    const val BAD_CLIENT = "bad-client"
    const val BAD_SERVER = "bad-server"
    const val OK = "ok"
}
