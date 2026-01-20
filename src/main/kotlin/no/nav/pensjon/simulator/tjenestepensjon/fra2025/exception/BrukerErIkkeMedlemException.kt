package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

import no.nav.pensjon.simulator.tech.selftest.SelfTest.Companion.APPLICATION_NAME
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics.TpSimuleringResultatFra2025

class BrukerErIkkeMedlemException : RuntimeException(), MetricAware {
    override val message: String
        get() = "Bruker er ikke medlem av en offentlig tjenestepensjonsordning"
    override val metricResult = TpSimuleringResultatFra2025.IKKE_MEDLEM
    override val metricSource = APPLICATION_NAME
}