package no.nav.pensjon.simulator.statistikk

import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StatistikkService(
    private val statistikkRepository: StatistikkRepository,
    private val snapshotRepository: SnapshotRepository,
    private val time: Time
) {
    private var snapshotAarMaaned: Int? = null

    fun registrer(hendelse: SimuleringHendelse) {
        statistikkRepository.update(hendelse)
    }

    fun hent(): List<SimuleringStatistikk> =
        statistikkRepository.read()

    @Synchronized
    fun takeSnapshotIfNeeded() {
        val currentAarMaaned = currentAarMaaned()

        if (snapshotAarMaaned == null || currentAarMaaned > snapshotAarMaaned!!) {
            takeSnapshot(currentAarMaaned)
            snapshotAarMaaned = currentAarMaaned
        }
    }

    fun getSnapshot(aarMaaned: Int): List<SimuleringStatistikk> =
        snapshotRepository.read(aarMaaned)

    private fun takeSnapshot(aarMaaned: Int) {
        snapshotRepository.create(
            MaanedligStatistikk(
                aarMaaned,
                statistikk = statistikkRepository.read()
            )
        )
    }

    private fun currentAarMaaned(): Int =
        aarMaaned(time.today())

    private companion object {
        private fun aarMaaned(dato: LocalDate): Int =
            dato.year * 100 + dato.monthValue
    }
}
