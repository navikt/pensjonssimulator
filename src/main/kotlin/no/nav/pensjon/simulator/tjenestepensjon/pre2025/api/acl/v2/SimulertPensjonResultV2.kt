package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

data class SimulertPensjonResultV2(
    val tpnr: String? = null,
    val navnOrdning: String? = null,
    val inkluderteOrdninger: List<String>? = null,
    val leverandorUrl: String? = null,
    val utbetalingsperioder: List<UtbetalingsperiodeResultV2>? = null
)