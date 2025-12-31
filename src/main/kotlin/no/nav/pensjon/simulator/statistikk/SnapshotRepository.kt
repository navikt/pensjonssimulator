package no.nav.pensjon.simulator.statistikk

interface SnapshotRepository {

    fun create(snapshot: MaanedligStatistikk)

    fun read(aarMaaned: Int): List<SimuleringStatistikk>
}
