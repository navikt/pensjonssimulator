package no.nav.pensjon.simulator.statistikk.db

data class StatistikkSnapshotDto(
    val aarMaaned: Int,
    val statistikk: List<SimuleringStatistikkDto>
)
