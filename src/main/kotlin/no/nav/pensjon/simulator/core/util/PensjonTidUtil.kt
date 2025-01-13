package no.nav.pensjon.simulator.core.util

import java.time.LocalDate
import java.util.*

object PensjonTidUtil {
    /**
     * Antall år mellom opptjening og ferdig skattelignet inntekt
     */
    const val OPPTJENING_ETTERSLEP_ANTALL_AAR = 2

    /**
     * For å kunne ta ut pensjon før denne alder kreves at du har tjent opp en årlig alderspensjon som minimum tilsvarer folketrygdens minstepensjon
     */
    const val LEGACY_UBETINGET_PENSJONERINGSALDER_AAR = 67

    const val LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR: Long = 62

    fun ubetingetPensjoneringDato(foedselsdato: Date): Date =
        foersteDagIMaanedenEtterAngittAlder(foedselsdato, LEGACY_UBETINGET_PENSJONERINGSALDER_AAR)

    fun ubetingetPensjoneringDato(foedselsdato: LocalDate): LocalDate =
        foedselsdato.toNorwegianDateAtNoon().let { ubetingetPensjoneringDato(it).toNorwegianLocalDate() }

    private fun foersteDagIMaanedenEtterAngittAlder(foedselsdato: Date, alderAar: Int): Date =
        foersteDagIMaanedenEtterAngittAlder(foedselsdato, alderAar, alderMaaneder = 0)

    private fun foersteDagIMaanedenEtterAngittAlder(foedselsdato: Date, alderAar: Int, alderMaaneder: Int): Date =
        NorwegianCalendar.forDate(foedselsdato).also {
            it.add(Calendar.YEAR, alderAar)
            it.add(Calendar.MONTH, alderMaaneder + 1)
            it[Calendar.DAY_OF_MONTH] = 1
        }.time
}
