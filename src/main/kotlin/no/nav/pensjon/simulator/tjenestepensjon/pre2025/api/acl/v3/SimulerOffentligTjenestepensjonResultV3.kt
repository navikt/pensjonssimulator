package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

data class SimulerOffentligTjenestepensjonResultV3(
    val simulertPensjonListe: List<SimulertPensjonResultV3>? = null,
    val feilkode: FeilkodeV3? = null,
    val relevanteTpOrdninger: List<String>? = emptyList(),
)
