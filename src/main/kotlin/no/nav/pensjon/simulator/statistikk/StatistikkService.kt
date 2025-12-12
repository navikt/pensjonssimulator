package no.nav.pensjon.simulator.statistikk

import org.springframework.stereotype.Service

@Service
class StatistikkService(private val repository: StatistikkRepository) {

    fun registrer(hendelse: SimuleringHendelse) {
        repository.update(hendelse)
    }

    fun hent(): List<SimuleringStatistikk> =
        repository.read()
}
