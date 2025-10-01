package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException

interface TjenestepensjonFra2025Client {
    @Throws(TjenestepensjonSimuleringException::class)
    fun simuler(spec: SimulerTjenestepensjonRequestDto, tpNummer: String): Result<SimulertTjenestepensjon>
}