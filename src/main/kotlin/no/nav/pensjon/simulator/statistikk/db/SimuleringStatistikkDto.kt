package no.nav.pensjon.simulator.statistikk.db

data class SimuleringStatistikkDto(
    val hendelse: SimuleringHendelseDto,
    val antall: Int
)
