package no.nav.pensjon.simulator.core.util

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.setTimeToZero
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

    fun ubetingetPensjoneringDato(foedselDato: Date): Date =
        foersteDagIMaanedenEtterAngittAlder(foedselDato, LEGACY_UBETINGET_PENSJONERINGSALDER_AAR)

    fun ubetingetPensjoneringDato(foedselDato: LocalDate): LocalDate =
        fromLocalDate(foedselDato)?.let { ubetingetPensjoneringDato(it).toLocalDate() }!!

    private fun foersteDagIMaanedenEtterAngittAlder(foedselDato: Date, alderAar: Int): Date =
        foersteDagIMaanedenEtterAngittAlder(foedselDato, alderAar, alderMaaneder = 0)

    private fun foersteDagIMaanedenEtterAngittAlder(foedselDato: Date, alderAar: Int, alderMaaneder: Int): Date =
        Calendar.getInstance().apply {
            time = foedselDato
        }.also {
            it.add(Calendar.YEAR, alderAar)
            it.add(Calendar.MONTH, alderMaaneder + 1)
            it[Calendar.DAY_OF_MONTH] = 1
        }.let {
            setTimeToZero(it.time)
        }
}
