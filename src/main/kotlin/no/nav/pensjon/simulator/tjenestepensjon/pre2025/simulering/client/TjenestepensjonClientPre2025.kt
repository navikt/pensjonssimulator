package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec

interface TjenestepensjonClientPre2025 {

    fun getPrognose(
        spec: TjenestepensjonSimuleringPre2025Spec,
        tpNr: String
    ): SimulerOffentligTjenestepensjonResult
}