package no.nav.pensjon.simulator.tech.time

import no.nav.pensjon.simulator.tech.time.DateUtil.overlapperEndeloest
import java.time.LocalDate

data class Interval(val fom: LocalDate?, val tom: LocalDate?) {

    fun intersectsWith(fomDato: LocalDate?, tomDato: LocalDate?) =
        overlapperEndeloest(
            start1 = fomDato,
            slutt1 = tomDato,
            start2 = fom,
            slutt2 = tom,
            anseEnkeltDagSomOverlapp = true
        )
}