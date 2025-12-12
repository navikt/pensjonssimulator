package no.nav.pensjon.simulator.statistikk

interface StatistikkRepository {

    fun update(hendelse: SimuleringHendelse)

    fun read(): List<SimuleringStatistikk>
}
