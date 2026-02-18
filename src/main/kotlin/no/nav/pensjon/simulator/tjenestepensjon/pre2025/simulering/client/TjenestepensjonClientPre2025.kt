package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto

interface TjenestepensjonClientPre2025 {

    fun getPrognose(
        spec: TjenestepensjonSimuleringPre2025Spec,
        tpOrdning: TpOrdningFullDto // TODO use domain class, not DTO
    ): SimulerOffentligTjenestepensjonResult
}