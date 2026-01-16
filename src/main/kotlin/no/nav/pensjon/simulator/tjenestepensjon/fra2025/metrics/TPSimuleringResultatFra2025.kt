package no.nav.pensjon.simulator.tjenestepensjon.fra2025.metrics

enum class TPSimuleringResultatFra2025 {
    OK,
    IKKE_MEDLEM,
    TP_ORDNING_STOETTES_IKKE,
    TEKNISK_FEIL_FRA_TP_ORDNING,
    INGEN_UTBETALINGSPERIODER,
    TEKNISK_FEIL_I_NAV
}