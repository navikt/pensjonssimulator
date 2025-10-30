package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SettTrygdetidHelper
@Component
class TrygdetidAdjuster(private val time: Time) {

    /**
     * Begrenser sluttdato for trygdetidperioden med seneste startdato, slik at perioden slutter senest i går.
     */
    // PEN: SettTrygdetidHelper.conditionallyAdjustLastTrygdetidsgrunnlag
     fun conditionallyAdjustLastTrygdetidPeriode(periodeListe: List<TTPeriode>, tom: LocalDate?) {
        val idag = time.today()
        val twoYearsBeforeToday: Date = idag.minusYears(2).toNorwegianDateAtNoon() //TODO magic value 2

        // Periode-relevans:
        //                                                           nå
        //                                |<--------- 2 år --------->|-------
        // relevant:      --------<==============>-------------------|------- (isDateInPeriod)
        // relevant:      ---------------------<================............. (isAfterByDay)
        // ikke-relevant: ---<=========>-------------------------------------
        val relevantePerioder = periodeListe.filter {
            isDateInPeriod(twoYearsBeforeToday, it.fom, it.tom) ||
                    isAfterByDay(thisDate = it.fom, thatDate = twoYearsBeforeToday, allowSameDay = false)
        }

        if (relevantePerioder.isEmpty()) return

        denMedSenesteFom(periodeListe)?.apply { // NB: Using periodeListe, not relevantePerioder (same as in PEN)
            this.tom = tom.begrensetTilDagenFoer(idag).toNorwegianDateAtNoon()
            this.poengIUtAr = false
        }
    }

    private fun LocalDate?.begrensetTilDagenFoer(dato: LocalDate): LocalDate =
        if (this?.isBefore(dato) == true) this
        else dato.minusDays(1)

    private companion object {

        // PEN: PeriodisertInformasjonListeUtils.findLatest
        private fun denMedSenesteFom(list: List<TTPeriode>): TTPeriode? {
            if (list.isEmpty()) return null
            if (list.size == 1) return list[0]

            var result: TTPeriode? = null
            var latestDate: Date? = null

            for (element in list) {
                if (isAfterByDay(element.fom, latestDate, allowSameDay = false)) {
                    result = element
                    latestDate = element.fom
                }
            }

            return result
        }
    }
}