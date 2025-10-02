package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TjenestepensjonSimuleringException

interface TjenestepensjonFra2025Client {
    @Throws(TjenestepensjonSimuleringException::class)
    fun simuler(spec: SimulerOffentligTjenestepensjonFra2025SpecV1, tpNummer: String): Result<SimulertTjenestepensjon>
}