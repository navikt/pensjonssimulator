package no.nav.pensjon.simulator.statistikk

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer

data class SimuleringHendelse(
    val organisasjonsnummer: Organisasjonsnummer,
    val simuleringstype: SimuleringTypeEnum
)
