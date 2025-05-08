package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate

// SimulatorPensjonTidUtil + SimulatorUttaksalderUtil
object UttakUtil {

    val indexedUttakGrader = mapOf(
        UttakGradKode.P_80 to 0,
        UttakGradKode.P_60 to 1,
        UttakGradKode.P_50 to 2,
        UttakGradKode.P_40 to 3,
        UttakGradKode.P_20 to 4
    )

    /**
     * Uttaksdato er første dag i måneden etter "aldersdato".
     * "aldersdato" er datoen da aldersinnehaveren har en gitt alder (aldersdato = fødselsdato + alder)
     */
    fun uttakDato(foedselsdato: LocalDate, uttakAlder: Alder): LocalDate =
        foedselsdato.plusYears(uttakAlder.aar.toLong())
            .plusMonths((uttakAlder.maaneder + 1).toLong())
            .withDayOfMonth(1)

    fun uttakDatoKandidat(foedselDato: LocalDate, lavesteUttakAlder: Alder, antallMaaneder: Int): LocalDate =
        uttakDato(foedselDato, lavesteUttakAlder).plusMonths(antallMaaneder.toLong())

    /**
     * Gets a map of indexed uttaksgrader, excluding uttaksgrader greater than the given maxUttakGrad.
     * The index starts at zero.
     * Index zero represents the greatest uttaksgrad in the map.
     * The greatest index represents the smallest uttaksgrad (20 %).
     */
    fun indexedUttakGradSubmap(maxUttakGrad: UttakGradKode): Map<Int, UttakGradKode> {
        val indexShift = indexedUttakGrader[maxUttakGrad] ?: 0

        return indexedUttakGrader
            .filter { it.value >= indexShift }
            .map { (grad, index) -> index - indexShift to grad }
            .toMap()
    }
}
