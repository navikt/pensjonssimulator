package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025

class TpOrdningStoettesIkkeException(val tpOrdning: String) : RuntimeException(), MetricAware {
    override val message: String
        get() = "$tpOrdning støtter ikke simulering av tjenestepensjon v2025"
    override val metricResult = TPSimuleringResultatFra2025.TP_ORDNING_STOETTES_IKKE
    override val metricSource = tpOrdning
}