package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TpSimuleringResultatFra2025

class TomSimuleringFraTpOrdningException(val tpOrdning: String) : RuntimeException(), MetricAware {
    override val message: String
        get() = "tom liste eller manglende simulering fra $tpOrdning"
    override val metricResult = TpSimuleringResultatFra2025.INGEN_UTBETALINGSPERIODER
    override val metricSource = tpOrdning
}