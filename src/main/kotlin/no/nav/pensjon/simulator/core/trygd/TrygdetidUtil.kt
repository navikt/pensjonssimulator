package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import java.util.*

object TrygdetidUtil {

    // Extracted from SettTrygdetidHelper.findAntallArMedOpptjening
    fun antallAarMedOpptjening(
        opptjeningAarSet: SortedSet<Int>,
        aarSoekerFikkMinstealderForTrygdetid: Int,
        dagensDato: Date
    ): Int {
        if (opptjeningAarSet.size < 1) return 0

        val forrigeAar = getYear(getRelativeDateByYear(dagensDato, -1))

        return if (aarSoekerFikkMinstealderForTrygdetid > forrigeAar)
            0
        else
            opptjeningAarSet.subSet(aarSoekerFikkMinstealderForTrygdetid, forrigeAar).size
    }
}
