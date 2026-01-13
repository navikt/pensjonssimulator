package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.trygdetidPeriode
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagForUtenlandsperioderMapper
object UtlandPeriodeTrygdetidMapper {

    fun utlandTrygdetidsgrunnlag(periodeListe: MutableList<UtlandPeriode>): List<TrygdetidOpphold> {
        val oppholdListe = mutableListOf<TrygdetidOpphold>()
        val sortedList = periodeListe.sortedBy { it.fom }

        sortedList.forEachIndexed { index, periode ->
            if (index + 1 < periodeListe.size)
                oppholdListe.add(trygdetidsgrunnlag(periode, sortedList[index + 1]))
            else
                oppholdListe.add(trygdetidsgrunnlag(periode))
        }

        return oppholdListe
    }

    // PEN: createTrygdetidsgrunnlagForUtenlandsperioder
    fun utlandTrygdetidsgrunnlag(
        utlandPeriodeListe: MutableList<UtlandPeriode>,
        trygdetidsgrunnlagMedPensjonspoengListe: List<TrygdetidOpphold>
    ): List<TrygdetidOpphold> =
        merge(
            outerList = utlandPeriodeListe.map(::trygdetidsgrunnlag).sortedBy { it.periode.fom },
            innerList = trygdetidsgrunnlagMedPensjonspoengListe.sortedBy { it.periode.fom }
        )

    // PEN: extract from createTrygdetidsgrunnlagForUtenlandsperioder
    private fun merge(
        outerList: List<TrygdetidOpphold>,
        innerList: List<TrygdetidOpphold>
    ): List<TrygdetidOpphold> {
        val resultList: MutableList<TrygdetidOpphold> = mutableListOf()
        var inner: TrygdetidOpphold
        var outer: TrygdetidOpphold
        var innerIndex = 0

        outerLoop@ for (outerIndex in outerList.indices) {
            outer = outerList[outerIndex]

            innerLoop@ while (innerIndex < innerList.size) {
                inner = innerList[innerIndex]

                if (endsBefore(outer, inner)) {
                    break@innerLoop
                } else if (startsBeforeAndEndsIn(outer, inner)) {
                    outer.periode.tom = dayBeforeStartOf(inner)
                    break@innerLoop
                } else if (startsAndEndsIn(outer, inner)) {
                    continue@outerLoop
                } else if (startsBeforeAndEndsAfter(outer, inner)) {
                    resultList.add(outer.withPeriodeTom(dato = dayBeforeStartOf(inner)))
                    outer.periode.fom = dayAfterEndOf(inner)
                } else if (endsBefore(inner, outer)) {
                    // No action
                } else if (startsInAndEndsAfter(outer, inner)) {
                    outer.periode.fom = dayAfterEndOf(inner)
                }

                innerIndex++
            }

            resultList.add(outer)
        }

        resultList.addAll(innerList)
        return resultList.sortedBy { it.periode.fom }
    }

    private fun trygdetidsgrunnlag(periode: UtlandPeriode, nestePeriode: UtlandPeriode) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fom,
                tom = periode.tom?.let { if (periode.tom == nestePeriode.fom) nestePeriode.fom.minusDays(1L) else periode.tom },
                land = periode.land,
                ikkeProRata = false,
                bruk = true
            ),
            arbeidet = periode.arbeidet
        )

    private fun trygdetidsgrunnlag(periode: UtlandPeriode) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fom,
                tom = periode.tom,
                land = periode.land,
                ikkeProRata = false,
                bruk = true
            ),
            arbeidet = periode.arbeidet
        )

    private fun dayBeforeStartOf(opphold: TrygdetidOpphold): Date? =
        opphold.periode.fom?.toNorwegianLocalDate()?.minusDays(1)?.toNorwegianDateAtNoon()

    private fun dayAfterEndOf(opphold: TrygdetidOpphold): Date? =
        opphold.periode.tom?.toNorwegianLocalDate()?.plusDays(1)?.toNorwegianDateAtNoon()

    private fun startsBeforeAndEndsIn(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isBeforeByDay(a.fom, b.fom, false)
                && a.tom != null && isBeforeByDay(b.fom, a.tom, allowSameDay = true)
                && isBeforeByDay(a.tom, b.tom, allowSameDay = true)
    }

    private fun startsBeforeAndEndsAfter(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isBeforeByDay(a.fom, b.fom, false)
                && (a.tom == null || isAfterByDay(a.tom, b.tom, allowSameDay = false))
    }

    private fun startsAndEndsIn(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isAfterByDay(a.fom, b.fom, true)
                && a.tom != null && isBeforeByDay(a.tom, b.tom, allowSameDay = true)
    }

    private fun startsInAndEndsAfter(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isAfterByDay(a.fom, b.fom, allowSameDay = true)
                && isBeforeByDay(a.fom, b.tom, allowSameDay = true)
                && (a.tom == null || isAfterByDay(a.tom, b.tom, allowSameDay = false))
    }

    private fun endsBefore(a: TrygdetidOpphold, b: TrygdetidOpphold): Boolean =
        a.periode.tom?.let { endsBefore(it, b.periode.fom) } == true

    private fun endsBefore(a: Date, b: Date?): Boolean =
        isBeforeByDay(a, b, allowSameDay = false)
}
