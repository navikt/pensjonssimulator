package no.nav.pensjon.simulator.statistikk.db

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum

/**
 * Needed for JSON serialization of organisasjonsnummer (cannot serialize inline value class properly).
 */
data class SimuleringHendelseDto(
    val organisasjonsnummer: String,
    val simuleringstype: SimuleringTypeEnum
)
