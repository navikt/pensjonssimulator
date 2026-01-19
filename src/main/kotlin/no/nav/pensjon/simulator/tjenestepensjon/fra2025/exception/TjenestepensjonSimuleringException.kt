package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025

class TjenestepensjonSimuleringException(val msg: String? = null, val tpOrdning: String) : RuntimeException(), MetricAware {
    override val message: String
        get() = "Feil ved simulering av tjenestepensjon ${msg ?: ""}"
    override val metricResult = TPSimuleringResultatFra2025.TEKNISK_FEIL_FRA_TP_ORDNING
    override val metricSource = tpOrdning
}