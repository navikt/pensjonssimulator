package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.util.*
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.trygdetidPeriode
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagWithArbeid
data class TrygdetidOpphold(
    val periode: TTPeriode,
    val arbeidet: Boolean
) {
    fun withPeriodeTom(dato: LocalDate?) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fomLd,
                tom = dato,
                land = periode.landEnum
            ),
            arbeidet
        )

    fun dayBefore(): Date? =
        periode.fomLd?.minusDays(1)?.toNorwegianDateAtNoon()

    fun dagenFoer(): LocalDate? =
        periode.fomLd?.minusDays(1)

    fun dayAfter(): Date? =
        periode.tomLd?.plusDays(1)?.toNorwegianDateAtNoon()

    fun dagenEtter(): LocalDate? =
        periode.tomLd?.plusDays(1)

    fun endsBefore(other: TrygdetidOpphold): Boolean =
        periode.tomLd?.isBeforeLd(other.periode.fomLd) == true

    fun startsBeforeAndEndsIn(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fomLd!!.isBeforeLd(b.fomLd)
                && a.tomLd != null && b.fomLd!!.isBeforeOrSame(a.tomLd)
                && a.tomLd!!.isBeforeOrSame(b.tomLd)
    }

    fun startsBeforeAndEndsAfter(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fomLd!!.isBeforeLd(b.fomLd)
                && (a.tomLd == null || a.tomLd!!.isAfterLd(b.tomLd))
    }

    fun startsAndEndsIn(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fomLd!!.isAfterOrSame(b.fomLd)
                && a.tomLd != null && a.tomLd!!.isBeforeOrSame(b.tomLd)
    }

    fun startsInAndEndsAfter(other: TrygdetidOpphold): Boolean {
        val a = periode
        val b = other.periode

        return a.fomLd!!.isAfterOrSame(b.fomLd)
                && a.fomLd!!.isBeforeOrSame(b.tomLd)
                && (a.tomLd == null || a.tomLd!!.isAfterLd(b.tomLd))
    }
}
