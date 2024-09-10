package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.toLocalDate
import java.time.LocalDate

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagForUtenlandsperioderMapper
object UtlandPeriodeTrygdetidMapper {

    fun utlandTrygdetidGrunnlag(utlandPeriodeListe: MutableList<UtlandPeriode>) =
        utlandPeriodeListe.map(::trygdetidGrunnlag)

    fun utlandTrygdetidGrunnlag(
        inputUtlandPeriodeListe: MutableList<UtlandPeriode>,
        trygdetidGrunnlagMedPensjonspoengListe: List<TrygdetidOpphold>
    ): List<TrygdetidOpphold> {
        val resultList: MutableList<TrygdetidOpphold> = mutableListOf()
        val utlandPeriodeListe =
            utlandTrygdetidGrunnlag(inputUtlandPeriodeListe).sortedBy { it.periode.fom }
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
                    utlandPeriode.periode.tom = fromLocalDate(findDayBeforeStartOfPeriod(poengPeriode))
                    break@innenlandsLoop
                } else if (startsAndEndsIn(utlandPeriode, poengPeriode)) {
                    continue@utenlandsLoop
                } else if (startsBeforeAndEndsAfter(utlandPeriode, poengPeriode)) {
                    val utenlandsperiodeCopy = copy(utlandPeriode).apply {
                        periode.tom = fromLocalDate(findDayBeforeStartOfPeriod(poengPeriode))
                    }
                    resultList.add(utenlandsperiodeCopy)
                    utlandPeriode.periode.fom = fromLocalDate(findDayAfterEndOfPeriod(poengPeriode))
                } else if (endsBefore(poengPeriode, utlandPeriode)) {
                    // No action
                } else if (startsInAndEndsAfter(utlandPeriode, poengPeriode)) {
                    utlandPeriode.periode.fom = fromLocalDate(findDayAfterEndOfPeriod(poengPeriode))
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
            land = opprinnelig.land?.let { Land.valueOf(it.kode) }
        )

        return TrygdetidOpphold(trygdetidPeriode, utlandPeriode.arbeidet)
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
        grunnlag.periode.fom.toLocalDate()?.minusDays(1)

    private fun findDayAfterEndOfPeriod(grunnlag: TrygdetidOpphold): LocalDate? =
        grunnlag.periode.tom.toLocalDate()?.plusDays(1)

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
