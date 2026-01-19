package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025

interface MetricAware {
    val metricResult: TPSimuleringResultatFra2025
    val metricSource: String
}
