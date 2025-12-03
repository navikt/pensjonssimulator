package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode

data class SimulerOffentligTjenestepensjonResultV2(
    val simulertPensjonListe: List<SimulertPensjonResultV2>? = null,
    val feilrespons: SimulerOFTPErrorResponseV2? = null,
)

data class SimulerOFTPErrorResponseV2(
    val errorCode: Feilkode,
    val errorMessage: String
)