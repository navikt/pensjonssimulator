package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TpSimuleringResultatFra2025

interface MetricAware {
    val metricResult: TpSimuleringResultatFra2025
    val metricSource: String
}
