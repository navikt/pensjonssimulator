package no.nav.pensjon.simulator.statistikk

data class MaanedligStatistikk(
    val aarMaaned: Int,
    val statistikk: List<SimuleringStatistikk>
)
