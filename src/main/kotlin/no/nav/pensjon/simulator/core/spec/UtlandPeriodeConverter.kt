package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Konverterer en liste av utenlandsperioder til antall år som periodene samlet utgjør.
 * Det regnes ikke med tidsrom før søkeren oppnår minstealder for trygdetid.
 * Det forutsettes at periodene ikke overlapper.
 * Det tas ingen spesielle hensyn til skuddår.
 * Maksimum antall år er 60.
 */
object UtlandPeriodeConverter {

    private const val TRYGDETID_MINSTEALDER_AAR = 16
    private const val MINSTE_ANTALL_DAGER_PER_AAR = 365
    private const val MAXIMUM_UTENLANDSOPPHOLD_ANTALL_AAR = 60 // presumably a reasonable value

    /**
     * Samlet antall år i angitte perioder etter minimumsalder for trygdetid.
     */
    fun limitedAntallAar(periodeListe: List<UtlandPeriode>, foedselsdato: LocalDate): Int =
        antallAar(periodeListe, foedselsdato).toInt().coerceAtMost(MAXIMUM_UTENLANDSOPPHOLD_ANTALL_AAR)

    private fun antallAar(periodeListe: List<UtlandPeriode>, foedselsdato: LocalDate): Long =
        antallDager(periodeListe, foedselsdato).coerceAtLeast(0) / MINSTE_ANTALL_DAGER_PER_AAR

    private fun antallDager(periodeListe: List<UtlandPeriode>, foedselsdato: LocalDate): Long =
        antallDager(periodeListe) - antallDagerFoerTidligsteTrygdetidDato(periodeListe, foedselsdato)

    private fun antallDager(periodeListe: List<UtlandPeriode>) =
        // pluss 1 dag p.g.a. 'til og med'
        periodeListe.sumOf { ChronoUnit.DAYS.between(it.fom, periodeTom(it).plusDays(1)) }

    private fun antallDagerFoerTidligsteTrygdetidDato(
        periodeListe: List<UtlandPeriode>,
        foedselsdato: LocalDate
    ): Long {
        val tidligsteFom = periodeListe.minOfOrNull { it.fom }
        if (tidligsteFom == null) return 0

        val startDato = tidligsteTrygdetidDato(foedselsdato).coerceAtLeast(tidligsteFom)
        return ChronoUnit.DAYS.between(tidligsteFom, startDato)
    }

    private fun tidligsteTrygdetidDato(foedselsdato: LocalDate): LocalDate =
        foedselsdato.plusYears(TRYGDETID_MINSTEALDER_AAR.toLong())

    /**
     * Utleder 'endelig' sluttdato. Håndterer disse tilfellene:
     * - sluttdato er udefinert
     * - sluttdato er før startdato
     */
    private fun periodeTom(periode: UtlandPeriode): LocalDate =
        periode.tom?.coerceAtLeast(periode.fom) // sluttdato kan ikke være før startdato
            ?: periode.fom.plusYears(MAXIMUM_UTENLANDSOPPHOLD_ANTALL_AAR.toLong()) // default-verdi hvis udefinert
}
