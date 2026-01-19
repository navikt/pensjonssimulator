package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tech.selftest.SelfTest.Companion.APPLICATION_NAME
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TPSimuleringResultatFra2025

class UgyldigSpecException(override val message: String) : RuntimeException(message), MetricAware {
    override val metricResult = TPSimuleringResultatFra2025.TEKNISK_FEIL_I_NAV
    override val metricSource = APPLICATION_NAME
}
