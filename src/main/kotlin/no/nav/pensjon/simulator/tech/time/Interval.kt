package no.nav.pensjon.simulator.tech.time

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersectsWithPossiblyOpenEndings
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate
import java.util.Date

data class Interval(val fom: LocalDate?, val tom: LocalDate?) {
    private val fomDate2 = fom?.toNorwegianDateAtNoon()
    private val tomDate2 = tom?.toNorwegianDateAtNoon()

    fun intersectsWith(fomDate1: Date?, tomDate1: Date?) =
        intersectsWithPossiblyOpenEndings(
            o1Start = fomDate1,
            o1End = tomDate1,
            o2Start = fomDate2,
            o2End = tomDate2,
            considerContactByDayAsIntersection = true
        )
}
