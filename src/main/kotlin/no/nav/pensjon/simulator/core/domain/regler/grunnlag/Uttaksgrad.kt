package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.util.*

// 2025-06-06, plus functions, minus compareTo
class Uttaksgrad {
    var fomDato: Date? = null
    var tomDato: Date? = null
    var uttaksgrad = 0

    /**
     * (Ref. PEN: CommonToReglerMapper.mapUttaksgradToRegler)
     */
    fun setDatesToNoon() {
        fomDato = fomDato?.noon()
        tomDato = tomDato?.noon()
    }

    fun tasUt(dato: LocalDate): Boolean =
        uttaksgrad > 0 && gjelder(dato)

    fun tattUtFoer(dato: LocalDate): Boolean =
        uttaksgrad > 0
                && isBeforeByDay(thisDate = fomDato, thatDate = dato, allowSameDay = false)
                && gjelder(dato).not()

    private fun gjelder(dato: LocalDate): Boolean {
        val fom: LocalDate? = fomDato?.toNorwegianLocalDate()
        val tom: LocalDate? = tomDato?.toNorwegianLocalDate()

        return if (isAfterByDay(thisDate = dato, thatDate = fom, allowSameDay = true))
            tom == null || isBeforeByDay(thisDate = dato, thatDate = tom, allowSameDay = true)
        else
            false
    }
}
