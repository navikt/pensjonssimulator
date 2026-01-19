package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025

class IkkeSisteOrdningException(val tpOrdning: String) : RuntimeException(), MetricAware {
    override val message: String
        get() = "$tpOrdning er ikke siste ordning"
    override val metricResult = TPSimuleringResultatFra2025.INGEN_UTBETALINGSPERIODER
    override val metricSource = tpOrdning
}