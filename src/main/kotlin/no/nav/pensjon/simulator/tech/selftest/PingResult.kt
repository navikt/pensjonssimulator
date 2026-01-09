package no.nav.pensjon.simulator.tech.selftest

import no.nav.pensjon.simulator.tech.security.egress.config.EgressService

data class PingResult(
    val service: EgressService,
    val status: ServiceStatus,
    val endpoint: String,
    val message: String
)
