package no.nav.pensjon.simulator.statistikk

import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.time.*
import org.springframework.stereotype.Service

@Service
class TertialstatistikkService(
    private val snapshotRepository: SnapshotRepository,
    private val time: Time
) {
    /**
     * Snapshot tas rett etter månedsskifte, så for å finne antall for f.eks. mars måned,
     * så tar man differansen mellom snapshot for april ("neste") og snapshot for mars.
     */
    fun antallPerMaaned(tertial: Int): Map<Kalendermaaned, AntallCombo> {
        val aar = time.today().year
        val kalendermaaneder = Tertial.kalendermaaneder(tertial)
        val aarMaanedListe: List<AarMaaned> = aarMaanedListe(aar, kalendermaaneder)
        val absolutteAntall: Map<AarMaaned, AntallCombo> = hentSnapshots(aarMaanedListe)

        return kalendermaaneder
            .map { AarMaaned(aar, maaned = it) }
            .associateBy(
                keySelector = { it.maaned },
                valueTransform = {
                    absolutteAntall.getValue(it.neste()) minus absolutteAntall.getValue(it)
                })
    }

    private fun hentSnapshots(aarMaanedListe: List<AarMaaned>): Map<AarMaaned, AntallCombo> =
        aarMaanedListe.associateBy(
            keySelector = { it },
            valueTransform = { antall(snapshot = snapshotRepository.read(aarMaaned = it.asInt)) }
        )

    private companion object {

        /**
         * Lager en liste over alle måneder som man skal hente statistikk for fra databasen.
         */
        private fun aarMaanedListe(
            aar: Int,
            kalendermaaneder: List<Kalendermaaned>
        ): List<AarMaaned> {
            val aarMaanedListe: MutableList<AarMaaned> = mutableListOf(
                AarMaaned(aar, maaned = kalendermaaneder[0]) // baseline
            )

            // Påfølgende måneder:
            kalendermaaneder.forEach {
                aarMaanedListe.add(AarMaaned(aar, maaned = it).neste())
            }

            return aarMaanedListe
        }

        private fun antall(snapshot: List<SimuleringStatistikk>) =
            AntallCombo(
                navAntall = navAntall(snapshot),
                totaltAntall = snapshot.sumOf { it.antall }
            )

        private fun navAntall(snapshot: List<SimuleringStatistikk>): Int =
            snapshot
                .filter { it.hendelse.organisasjonsnummer == Organisasjoner.nav }
                .sumOf { it.antall }
    }
}