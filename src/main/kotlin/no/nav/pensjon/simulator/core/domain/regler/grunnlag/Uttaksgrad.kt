package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import java.time.LocalDate

// 2026-04-23, plus functions, minus compareTo
class Uttaksgrad {
    var fomDatoLd: LocalDate? = null
    var tomDatoLd: LocalDate? = null
    var uttaksgrad = 0

    // Extra:

    fun tasUt(dato: LocalDate): Boolean =
        uttaksgrad > 0 && gjelder(dato)

    fun tattUtFoer(dato: LocalDate): Boolean =
        uttaksgrad > 0
                && isBeforeByDay(thisDate = fomDatoLd, thatDate = dato, allowSameDay = false)
                && gjelder(dato).not()

    private fun gjelder(dato: LocalDate): Boolean {
        val fom: LocalDate? = fomDatoLd
        val tom: LocalDate? = tomDatoLd

        return if (isAfterByDay(thisDate = dato, thatDate = fom, allowSameDay = true))
            tom == null || isBeforeByDay(thisDate = dato, thatDate = tom, allowSameDay = true)
        else
            false
    }
    // end extra
}
