package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.util.*
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.trygdetidPeriode
import java.util.*

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagWithArbeid
data class TrygdetidOpphold(
    val periode: TTPeriode,
    val arbeidet: Boolean
) {
    fun withPeriodeTom(dato: Date?) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fom,
                tom = dato,
                land = periode.landEnum
            ),
            arbeidet
        )

    fun dayBefore(): Date? =
        periode.fom?.toNorwegianLocalDate()?.minusDays(1)?.toNorwegianDateAtNoon()

    fun dayAfter(): Date? =
        periode.tom?.toNorwegianLocalDate()?.plusDays(1)?.toNorwegianDateAtNoon()

    fun endsBefore(other: TrygdetidOpphold): Boolean =
        periode.tom?.isBefore(other.periode.fom) == true

    fun startsBeforeAndEndsIn(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fom!!.isBefore(b.fom)
                && a.tom != null && b.fom!!.isBeforeOrSame(a.tom)
                && a.tom!!.isBeforeOrSame(b.tom)
    }

    fun startsBeforeAndEndsAfter(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fom!!.isBefore(b.fom)
                && (a.tom == null || a.tom!!.isAfter(b.tom))
    }

    fun startsAndEndsIn(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fom!!.isAfterOrSame(b.fom)
                && a.tom != null && a.tom!!.isBeforeOrSame(b.tom)
    }

    fun startsInAndEndsAfter(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fom!!.isAfterOrSame(b.fom)
                && a.fom!!.isBeforeOrSame(b.tom)
                && (a.tom == null || a.tom!!.isAfter(b.tom))
    }
}
