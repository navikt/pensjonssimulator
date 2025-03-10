package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagForUtenlandsperioderMapper
object UtlandPeriodeTrygdetidMapper {

    fun utlandTrygdetidGrunnlag(periodeListe: MutableList<UtlandPeriode>): List<TrygdetidOpphold> {
        val oppholdListe = mutableListOf<TrygdetidOpphold>()
        val sortedList = periodeListe.sortedBy { it.fom }

        sortedList.forEachIndexed { index, periode ->
            if (index + 1 < periodeListe.size)
                oppholdListe.add(trygdetidGrunnlag(periode, sortedList[index + 1]))
            else
                oppholdListe.add(trygdetidGrunnlag(periode))
        }

        return oppholdListe
    }

    fun utlandTrygdetidGrunnlag(
        inputUtlandPeriodeListe: MutableList<UtlandPeriode>,
        trygdetidGrunnlagMedPensjonspoengListe: List<TrygdetidOpphold>
    ): List<TrygdetidOpphold> {
        val resultList: MutableList<TrygdetidOpphold> = mutableListOf()
        val utlandPeriodeListe = inputUtlandPeriodeListe.map(::trygdetidGrunnlag).sortedBy { it.periode.fom }
        val pensjonspoengListe = trygdetidGrunnlagMedPensjonspoengListe.sortedBy { it.periode.fom }
        var poengPeriode: TrygdetidOpphold?
        var utlandPeriode: TrygdetidOpphold?
        var innerIndex = 0

        utenlandsLoop@ for (outerIndex in utlandPeriodeListe.indices) {
            utlandPeriode = utlandPeriodeListe[outerIndex]
            innenlandsLoop@ while (innerIndex < pensjonspoengListe.size) {
                poengPeriode = pensjonspoengListe[innerIndex]

                if (endsBefore(utlandPeriode, poengPeriode)) {
                    break@innenlandsLoop
                } else if (startsBeforeAndEndsIn(utlandPeriode, poengPeriode)) {
                    utlandPeriode.periode.tom = findDayBeforeStartOfPeriod(poengPeriode)?.toNorwegianDateAtNoon()
                    break@innenlandsLoop
                } else if (startsAndEndsIn(utlandPeriode, poengPeriode)) {
                    continue@utenlandsLoop
                } else if (startsBeforeAndEndsAfter(utlandPeriode, poengPeriode)) {
                    val utenlandsperiodeCopy = copy(utlandPeriode).apply {
                        periode.tom = findDayBeforeStartOfPeriod(poengPeriode)?.toNorwegianDateAtNoon()
                    }
                    resultList.add(utenlandsperiodeCopy)
                    utlandPeriode.periode.fom = findDayAfterEndOfPeriod(poengPeriode)?.toNorwegianDateAtNoon()
                } else if (endsBefore(poengPeriode, utlandPeriode)) {
                    // No action
                } else if (startsInAndEndsAfter(utlandPeriode, poengPeriode)) {
                    utlandPeriode.periode.fom = findDayAfterEndOfPeriod(poengPeriode)?.toNorwegianDateAtNoon()
                }

                innerIndex++
            }

            resultList.add(utlandPeriode)
        }

        resultList.addAll(pensjonspoengListe)
        return resultList.sortedBy { it.periode.fom }
    }

    private fun copy(utlandPeriode: TrygdetidOpphold): TrygdetidOpphold {
        val opprinnelig = utlandPeriode.periode

        val trygdetidPeriode = TrygdetidGrunnlagFactory.trygdetidPeriode(
            fom = opprinnelig.fom,
            tom = opprinnelig.tom,
            land = opprinnelig.landEnum
        )

        return TrygdetidOpphold(trygdetidPeriode, utlandPeriode.arbeidet)
    }

    private fun trygdetidGrunnlag(periode: UtlandPeriode, nestePeriode: UtlandPeriode): TrygdetidOpphold {
        val trygdetidPeriode: TTPeriode = TrygdetidGrunnlagFactory.trygdetidPeriode(
            fom = periode.fom,
            tom = periode.tom?.let { if (periode.tom == nestePeriode.fom) nestePeriode.fom.minusDays(1L) else periode.tom },
            land = periode.land,
            ikkeProRata = false,
            bruk = true
        )

        return TrygdetidOpphold(trygdetidPeriode, periode.arbeidet)
    }

    private fun trygdetidGrunnlag(utlandPeriode: UtlandPeriode): TrygdetidOpphold {
        val trygdetidPeriode = TrygdetidGrunnlagFactory.trygdetidPeriode(
            fom = utlandPeriode.fom,
            tom = utlandPeriode.tom,
            land = utlandPeriode.land,
            ikkeProRata = false,
            bruk = true
        )

        return TrygdetidOpphold(trygdetidPeriode, utlandPeriode.arbeidet)
    }

    private fun findDayBeforeStartOfPeriod(grunnlag: TrygdetidOpphold): LocalDate? =
        grunnlag.periode.fom?.toNorwegianLocalDate()?.minusDays(1)

    private fun findDayAfterEndOfPeriod(grunnlag: TrygdetidOpphold): LocalDate? =
        grunnlag.periode.tom?.toNorwegianLocalDate()?.plusDays(1)

    private fun startsBeforeAndEndsIn(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isBeforeByDay(a.fom, b.fom, false)
                && a.tom != null && isBeforeByDay(b.fom, a.tom, true)
                && isBeforeByDay(a.tom, b.tom, true)
    }

    private fun startsBeforeAndEndsAfter(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isBeforeByDay(a.fom, b.fom, false)
                && (a.tom == null || isAfterByDay(a.tom, b.tom, false))
    }

    private fun startsAndEndsIn(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isAfterByDay(a.fom, b.fom, true)
                && a.tom != null && isBeforeByDay(a.tom, b.tom, true)
    }

    private fun startsInAndEndsAfter(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return isAfterByDay(a.fom, b.fom, true)
                && isBeforeByDay(a.fom, b.tom, true)
                && (a.tom == null || isAfterByDay(a.tom, b.tom, false))
    }

    private fun endsBefore(grunnlagA: TrygdetidOpphold, grunnlagB: TrygdetidOpphold): Boolean {
        val a = grunnlagA.periode
        val b = grunnlagB.periode

        return a.tom != null && isBeforeByDay(a.tom, b.fom, false)
    }
}
