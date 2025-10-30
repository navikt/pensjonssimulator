package no.nav.pensjon.simulator.trygdetid

import java.time.LocalDate
import java.util.*

object TrygdetidUtil {

    // Extracted from SettTrygdetidHelper.findAntallArMedOpptjening
    fun antallAarMedOpptjening(
        registrerteAarMedOpptjening: SortedSet<Int>,
        aarSoekerFikkMinstealderForTrygdetid: Int,
        dagensDato: LocalDate
    ): Int {
        if (registrerteAarMedOpptjening.size < 1) return 0

        val forrigeAar = dagensDato.year - 1

        return if (aarSoekerFikkMinstealderForTrygdetid > forrigeAar)
            0
        else
            registrerteAarMedOpptjening.subSet(aarSoekerFikkMinstealderForTrygdetid, forrigeAar).size
    }
}