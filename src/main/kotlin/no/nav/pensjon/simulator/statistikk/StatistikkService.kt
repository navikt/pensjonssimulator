package no.nav.pensjon.simulator.statistikk

import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.time.Kalendermaaned
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.collections.mapOf

@Service
class StatistikkService(
    private val statistikkRepository: StatistikkRepository,
    private val snapshotRepository: SnapshotRepository,
    private val time: Time
) {
    private var snapshotAarMaaned: Int? = null

    private val kalendermaanederPerTertial: Map<Int, List<Kalendermaaned>> = mapOf(
        1 to listOf(
            Kalendermaaned(1, "januar"),
            Kalendermaaned(2, "februar"),
            Kalendermaaned(3, "mars"),
            Kalendermaaned(4, "april")
        ),
        2 to listOf(
            Kalendermaaned(5, "mai"),
            Kalendermaaned(6, "juni"),
            Kalendermaaned(7, "juli"),
            Kalendermaaned(8, "august")
        ),
        3 to listOf(
            Kalendermaaned(9, "september"),
            Kalendermaaned(10, "oktober"),
            Kalendermaaned(11, "november"),
            Kalendermaaned(12, "desember")
        )
    )

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

    fun antallPerMaaned(tertial: Int): Map<Kalendermaaned, AntallCombo> {
        val antallPerMaaned: MutableMap<Kalendermaaned, AntallCombo> = mutableMapOf()
        val aar = time.today().year

        kalendermaanederPerTertial[tertial]!!.forEach {
            antallPerMaaned[it] = antall(aar, kalendermaaned = it)
        }

        return antallPerMaaned
    }

    private fun antall(aar: Int, kalendermaaned: Kalendermaaned): AntallCombo {
        val forrigeMaanedsAar = if (kalendermaaned.nummer == 1) aar - 1 else aar
        val naavaerendeAarMaaned = aar * 100 + kalendermaaned.nummer
        val forrigeAarMaaned = forrigeMaanedsAar * 100 + kalendermaaned.forrigeNummer
        val naavaerendeStatistikk: List<SimuleringStatistikk> = snapshotRepository.read(naavaerendeAarMaaned)
        val forrigeStatistikk: List<SimuleringStatistikk> = snapshotRepository.read(forrigeAarMaaned)

        return AntallCombo(
            navAntall = navAntall(naavaerendeStatistikk) - navAntall(forrigeStatistikk),
            totaltAntall = naavaerendeStatistikk.sumOf { it.antall } - forrigeStatistikk.sumOf { it.antall }
        )
    }

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

        private fun navAntall(statistikk: List<SimuleringStatistikk>): Int =
            statistikk
                .filter { it.hendelse.organisasjonsnummer == Organisasjoner.nav }
                .sumOf { it.antall }
    }
}