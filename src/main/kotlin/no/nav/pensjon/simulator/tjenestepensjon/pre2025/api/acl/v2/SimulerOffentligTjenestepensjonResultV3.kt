package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode

data class SimulerOffentligTjenestepensjonResultV3(
    val simulertPensjonListe: List<SimulertPensjonResultV3>? = null,
    val feilkode: Feilkode? = null,
    val relevanteTpOrdninger: List<String>? = emptyList(),
)