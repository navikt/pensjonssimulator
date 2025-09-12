package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import java.time.LocalDate
import java.util.*

object TrygdetidUtil {

    // Extracted from SettTrygdetidHelper.findAntallArMedOpptjening
    fun antallAarMedOpptjening(
        opptjeningAarSet: SortedSet<Int>,
        aarSoekerFikkMinstealderForTrygdetid: Int,
        dagensDato: LocalDate
    ): Int {
        if (opptjeningAarSet.size < 1) return 0

        val forrigeAar = dagensDato.year - 1

        return if (aarSoekerFikkMinstealderForTrygdetid > forrigeAar)
            0
        else
            opptjeningAarSet.subSet(aarSoekerFikkMinstealderForTrygdetid, forrigeAar).size
    }
}