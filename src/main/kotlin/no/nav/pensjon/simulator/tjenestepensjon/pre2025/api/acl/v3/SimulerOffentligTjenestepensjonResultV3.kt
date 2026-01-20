package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

data class SimulerOffentligTjenestepensjonResultV3(
    val simulertPensjonListe: List<SimulertPensjonResultV3>? = null,
    val feilkode: FeilkodeV3? = null,
    val relevanteTpOrdninger: List<String>? = emptyList()
)

enum class FeilkodeV3 {
    TEKNISK_FEIL,
    BEREGNING_GIR_NULL_UTBETALING,
    OPPFYLLER_IKKE_INNGANGSVILKAAR,
    BRUKER_IKKE_MEDLEM_AV_TP_ORDNING,
    TP_ORDNING_STOETTES_IKKE,
    ANNEN_KLIENTFEIL
}