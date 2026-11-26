package no.nav.pensjon.simulator.tjenestepensjon.pre2025

data class TjenestepensjonSimuleringsresultatVedFeil(
    val utbetalingsperiodeListe: List<Utbetalingsperiode> = emptyList(),
    val feilkode: Feilkode
)