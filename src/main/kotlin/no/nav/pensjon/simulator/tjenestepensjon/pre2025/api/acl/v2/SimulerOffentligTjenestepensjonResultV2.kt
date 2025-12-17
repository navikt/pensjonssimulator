package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode

data class SimulerOffentligTjenestepensjonResultV2(
    val simulertPensjonListe: List<SimulertPensjonResultV2>? = null,
    val feilkode: Feilkode? = null,
)