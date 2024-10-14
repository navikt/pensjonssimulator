package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.search.IntegerDiscriminator
import no.nav.pensjon.simulator.search.SearchForMinimum
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate

// SimulatorPensjonTidUtil + SimulatorUttaksalderUtil
object UttakUtil {
    /**
     * Antall år fra alder når alderspensjonen tidligst kan tas ut til alder der alle har ubetinget rett til å ta ut minsteytelsen.
     * Før 2026 var disse alderne henholdsvis 62 og 67 år.
     * F.o.m. 2026 vil alderne være dynamiske, men differansen vil være den samme (5 år).
     */
    const val ANTALL_AAR_MELLOM_NEDRE_ALDERSGRENSE_OG_NORMALDER = 5

    /**
     * Antall måneder fra alder når alderspensjonen tidligst kan tas ut til alder der alle har ubetinget rett til å ta ut minsteytelsen.
     * Før 2026 var disse alderne henholdsvis 62 og 67 år.
     * F.o.m. 2026 vil alderne være dynamiske, men differansen vil være den samme (5 år).
     */
    private const val ANTALL_KANDIDAT_MAANEDER = ANTALL_AAR_MELLOM_NEDRE_ALDERSGRENSE_OG_NORMALDER * MAANEDER_PER_AAR

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
    fun uttakDato(foedselDato: LocalDate, uttakAlder: Alder): LocalDate =
        foedselDato.plusYears(uttakAlder.aar.toLong())
            .plusMonths((uttakAlder.maaneder + 1).toLong())
            .withDayOfMonth(1)

    // In PEN used by SimulatorLavesteUttaksalderFinder (TMU)
    fun forsteUttakDato(
        foedselDato: LocalDate,
        tidligstUttakAlder: Alder,
        maxAlder: Alder,
        discriminator: IntegerDiscriminator
    ): LocalDate {
        val antallKandidatMaaneder = maxAlder.antallMaanederEtter(tidligstUttakAlder)
        val antallMaaneder = SearchForMinimum(discriminator).search(antallKandidatMaaneder)
        val tilleggMaaneder = if (antallMaaneder < 0) ANTALL_KANDIDAT_MAANEDER else antallMaaneder
        return uttakDato(foedselDato, tidligstUttakAlder).plusMonths(tilleggMaaneder.toLong())
    }

    fun uttakDatoKandidat(foedselDato: LocalDate, lavesteUttakAlder: Alder, antallMaaneder: Int): LocalDate =
        uttakDato(foedselDato, lavesteUttakAlder).plusMonths(antallMaaneder.toLong())

    //fun alderForrigeMaaned(alder: UttaksalderAlderDto) = Alder(alder.aar, alder.maaneder).minusMaaneder(1)

    /*V4 *
     * Finner høyeste sluttalder (til og med) for gradert uttak.
     * Det er alderen man har måneden før startalder for helt uttak.
     * /
    fun gradertUttakMaxTomAlder(spec: DatobasertUttakSpec, alderIfNotGradert: Alder): Alder {
        val alder: UttaksalderAlderDto = heltUttakFomAlder(spec, alderIfNotGradert)

        if (alder.aar < 0) {
            throw InvalidArgumentException("Ugyldig alder for helt uttak (f.o.m. = ${spec.gradertUttak?.heltUttakFom}) => år = ${alder.aar}")
        }

        return alderForrigeMaaned(alder)
    }
*/
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
    /*V4
        private fun heltUttakFomAlder(spec: DatobasertUttakSpec, alderIfNotGradert: Alder): UttaksalderAlderDto =
            spec.gradertUttak?.let {
                with(alderDato(spec.foedselDato, it.heltUttakFom).alder) {
                    UttaksalderAlderDto(aar = this.aar, maaneder = this.maaneder)
                }
            } ?: dto(alderIfNotGradert)
    */
}
