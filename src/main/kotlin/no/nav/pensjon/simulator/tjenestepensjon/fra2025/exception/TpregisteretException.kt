package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TpSimuleringResultatFra2025

class TpregisteretException(message: String) : RuntimeException(message), MetricAware {
    override val metricResult = TpSimuleringResultatFra2025.TEKNISK_FEIL_I_NAV
    override val metricSource = "tpregisteret"
}