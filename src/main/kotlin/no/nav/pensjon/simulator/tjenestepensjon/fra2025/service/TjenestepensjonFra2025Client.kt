package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon

interface TjenestepensjonFra2025Client {

    fun simuler(spec: OffentligTjenestepensjonFra2025SimuleringSpec, tpNummer: String): Result<SimulertTjenestepensjon>
    val service: EgressService
}