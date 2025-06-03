package no.nav.pensjon.simulator.core.legacy.util

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.util.NorwegianCalendar
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object DateUtil {

    val LOCAL_ETERNITY: LocalDate = LocalDate.of(9999, 12, 31)
    val ETERNITY: Date = LOCAL_ETERNITY.toNorwegianDateAtNoon()

    // SimuleringEtter2011Utils.monthOfYearRange1To12
    fun monthOfYearRange1To12(date: Date): Int =
        getMonth(date) + 1

    fun monthOfYearRange1To12(date: LocalDate): Int =
        monthOfYearRange1To12(date.toNorwegianDateAtNoon())

    fun dateIsValid(fom: Date?, tom: Date?, virkFom: LocalDate?, virkTom: LocalDate?) =
        intersectsWithPossiblyOpenEndings(
            o1Start = fom?.toNorwegianLocalDate(),
            o1End = tom?.toNorwegianLocalDate(),
            o2Start = virkFom,
            o2End = virkTom,
            considerContactByDayAsIntersection = true
        )

    /**
     * Removes the values for HOUR_OF_DAY, MINUTES, SECONDS and MILLISECONDS before the compare such
     * that same day is regarded as intersection if `considerContactByDayAsIntersection` is true. The endings of
     * the respective periods can be NULL, if so these will be set to infinity.
     */
    private fun intersectsWithPossiblyOpenEndings(
        o1Start: LocalDate?, o1End: LocalDate?, o2Start: LocalDate?, o2End: LocalDate?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean =
        intersectsWithPossiblyOpenEndings(
            o1Start?.toNorwegianDateAtNoon(),
            o1End?.toNorwegianDateAtNoon(),
            o2Start?.toNorwegianDateAtNoon(),
            o2End?.toNorwegianDateAtNoon(),
            considerContactByDayAsIntersection
        )

    fun intersectsWithPossiblyOpenEndings(
        o1Start: LocalDate?, o1End: LocalDate?, o2Start: Date?, o2End: Date?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean =
        intersectsWithPossiblyOpenEndings(
            o1Start?.toNorwegianDateAtNoon(),
            o1End?.toNorwegianDateAtNoon(),
            o2Start,
            o2End,
            considerContactByDayAsIntersection
        )

    // PEN: no.stelvio.common.util.DateUtil.intersectsWithPossiblyOpenEndings
    // NB: Here ETERNITY is used instead of Date(Long.MAX_VALUE)
    //     in order to avoid timezone problems
    fun intersectsWithPossiblyOpenEndings(
        o1Start: Date?, o1End: Date?, o2Start: Date?, o2End: Date?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean =
        intersects(
            o1Start,
            o1End ?: ETERNITY,
            o2Start,
            o2End ?: ETERNITY,
            considerContactByDayAsIntersection
        )

    /**
     * Removes the values for HOUR_OF_DAY, MINUTES, SECONDS and MILLISECONDS before the compare such
     * that same day is regarded as intersection if `considerContactByDayAsIntersection` is true.
     */
    fun intersects(
        o1Start: Date?, o1End: Date?, o2Start: Date?, o2End: Date?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean {
        val o1StartDay: Date = createDayCalendar(o1Start).getTime()
        val o1EndDay: Date = createDayCalendar(o1End).getTime()
        val o2StartDay: Date = createDayCalendar(o2Start).getTime()
        val o2EndDay: Date = createDayCalendar(o2End).getTime()

        return intersectsByMilliseconds(
            o1StartDay,
            o1EndDay,
            o2StartDay,
            o2EndDay,
            considerContactByDayAsIntersection
        )
    }

    /**
     * Check whether a closed date range intersects another closed date range. Will throw a NullPointerException if any of the
     * passed parameters are null. To check for intersection there are several cases to be covered:
     * <pre>
     *       |-------|          (the first period, called period 1)
     *       |-------|		  0 same period is an intersection
     * |---|                  1 no intersection with period 1
     *                 |----| 2 no intersection with period 1
     *  |-------|             3 intersection, it ends before period 1 ends
     *             |------|   4 intersection, it starts before period 1 ends
     * |----------------|     5 intersection, starts before and ends after period 1
     * |-----|				  6 Special case, ends when period 1 starts (by milliseconds)
     *               |------| 7 Special case, begins when period 1 ends  (by milliseconds)
     * |-------|          (the first period, called period 1)
     * |-------|		  0 same period is an intersection
     * |---|                  1 no intersection with period 1
     * |----| 2 no intersection with period 1
     * |-------|             3 intersection, it ends before period 1 ends
     * |------|   4 intersection, it starts before period 1 ends
     * |----------------|     5 intersection, starts before and ends after period 1
     * |-----|				  6 Special case, ends when period 1 starts (by milliseconds)
     *  </pre>
     */
    private fun intersectsByMilliseconds(
        o1Start: Date, o1End: Date, o2Start: Date, o2End: Date,
        considerContactAsIntersection: Boolean
    ): Boolean {
        val isPoint = o1Start == o1End || o2Start == o2End

        // get the max of starts
        val start = max(o1Start.time.toDouble(), o2Start.time.toDouble()).toLong()
        // get the min of ends
        val end = min(o1End.time.toDouble(), o2End.time.toDouble()).toLong()

        return if (considerContactAsIntersection || isPoint)
            start <= end
        else
            start < end
    }

    fun calculateAgeInYears(foedselsdato: Date, dato: LocalDate): Int =
        calculateAgeInYears(foedselsdato.toNorwegianLocalDate(), dato)

    // SimuleringEtter2011Utils.calculateAgeInYears
    // NB: Compare this with PensjonAlderDato.alderVedDato
    fun calculateAgeInYears(foedselsdato: LocalDate?, dato: LocalDate?): Int =
        foedselsdato?.let {
            Period.between(
                lastDayOfMonth(it),
                dato?.withDayOfMonth(1)
            ).years
        } ?: 0

    fun createDate(year: Int, month: Int, day: Int): Date =
        NorwegianCalendar.dateAtNoon(year, month, day)

    /**
     * Returns the number of months between two dates.
     */
    // no.stelvio.common.util.DateUtil.getMonthBetween
    fun getMonthBetween(fromDate: Date, toDate: Date): Int {
        val fromCalendar: Calendar = NorwegianCalendar.forNoon(fromDate)
        val toCalendar: Calendar = NorwegianCalendar.forNoon(toDate)

        val fromTotalMonths: Int =
            MAANEDER_PER_AAR * fromCalendar[Calendar.YEAR] + fromCalendar[Calendar.MONTH]
        val toTotalMonths: Int =
            MAANEDER_PER_AAR * toCalendar[Calendar.YEAR] + toCalendar[Calendar.MONTH]

        return abs((fromTotalMonths - toTotalMonths).toDouble()).toInt()
    }

    fun getMonthBetween(fromDate: LocalDate, toDate: LocalDate): Int =
        getMonthBetween(fromDate.toNorwegianDateAtNoon(), toDate.toNorwegianDateAtNoon())

    // no.stelvio.common.util.DateUtil.isDateInPeriod
    fun isDateInPeriod(dato: Date?, fom: Date?, tom: Date?): Boolean {
        if (fom == null || dato == null) {
            return false
        }

        var tomOk = false

        if (tom == null) {
            tomOk = true
        } else {
            if (isFirstDayBeforeSecond(dato, tom) || isSameDay(dato, tom)) {
                tomOk = true
            }
        }

        return tomOk && (isFirstDayBeforeSecond(fom, dato) || isSameDay(dato, fom))
    }

    fun isDateInPeriod(dato: LocalDate?, fom: Date?, tom: Date?): Boolean =
        isDateInPeriod(dato?.toNorwegianDateAtNoon(), fom, tom)

    /**
     * Returns true if the dates are the same day.
     * Hours, minutes, seconds and milliseconds are not regarded.
     */
    // no.stelvio.common.util.DateUtil.isSameDay
    fun isSameDay(a: Date?, b: Date?): Boolean =
        when {
            a == null && b == null -> false
            else -> createDayCalendar(a) == createDayCalendar(b)
        }

    fun isSameDay(a: LocalDate?, b: LocalDate?): Boolean =
        isSameDay(a?.toNorwegianDateAtNoon(), b?.toNorwegianDateAtNoon())

    fun isFirstDayBeforeSecond(first: Date, second: Date): Boolean {
        val firstCalendar: Calendar = NorwegianCalendar.forNoon(first)
        val secondCalendar: Calendar = NorwegianCalendar.forNoon(second)
        return firstCalendar.time.before(secondCalendar.time)
    }

    // SimuleringEtter2011Utils.firstDayOfMonthAfterUserTurnsGivenAge
    fun firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato: Date, alderAar: Int): Date =
        NorwegianCalendar.forNoon(foedselsdato).apply {
            add(Calendar.YEAR, alderAar)
            add(Calendar.MONTH, 1)
            this[Calendar.DAY_OF_MONTH] = 1
        }.time

    fun firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato: LocalDate, alderAar: Int): Date =
        firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato.toNorwegianDateAtNoon(), alderAar)

    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011Utils.lastDayOfMonthUserTurnsGivenAge
    fun lastDayOfMonthUserTurnsGivenAge(foedselsdato: Date, alder: Alder): Date {
        val dato: Date =
            NorwegianCalendar.forNoon(foedselsdato).apply {
                add(Calendar.YEAR, alder.aar)
                add(Calendar.MONTH, alder.maaneder + 1)
            }.time

        return findLastDayInMonthBefore(dato)
    }

    // no.stelvio.common.util.DateUtil.getFirstDayOfMonth
    fun getFirstDayOfMonth(date: Date): Date =
        getFirstOrLastDayOfMonth(date, true)

    // no.stelvio.common.util.DateUtil.getFirstOrLastDayOfMonth
    private fun getFirstOrLastDayOfMonth(date: Date, first: Boolean): Date =
        NorwegianCalendar.forNoon(date).apply {
            this[Calendar.DAY_OF_MONTH] = if (first)
                getActualMinimum(Calendar.DAY_OF_MONTH)
            else
                getActualMaximum(Calendar.DAY_OF_MONTH)
        }.time

    fun getLastDayOfMonth(date: Date): Date =
        getFirstOrLastDayOfMonth(date, false)

    // no.stelvio.common.util.DateUtil.getFirstDateInYear
    fun getFirstDateInYear(date: Date): Date =
        NorwegianCalendar.forNoon(date).apply {
            this[Calendar.MONTH] = Calendar.JANUARY
            this[Calendar.DAY_OF_MONTH] = getActualMinimum(Calendar.DAY_OF_MONTH)
        }.time

    //TODO replace by foersteDag
    fun getFirstDateInYear(date: LocalDate): LocalDate =
        getFirstDateInYear(date.toNorwegianDateAtNoon()).toNorwegianLocalDate()

    // no.stelvio.common.util.DateUtil.getLastDateInYear
    fun getLastDateInYear(date: Date): Date =
        NorwegianCalendar.forNoon(date).apply {
            this[Calendar.MONTH] = Calendar.DECEMBER
            this[Calendar.DAY_OF_MONTH] = getActualMaximum(Calendar.DAY_OF_MONTH)
        }.time

    fun getLastDateInYear(date: LocalDate): LocalDate =
        getLastDateInYear(date.toNorwegianDateAtNoon()).toNorwegianLocalDate()

    // SimuleringEtter2011Utils.yearUserTurnsGivenAge
    fun yearUserTurnsGivenAge(foedselsdato: Date, age: Int): Int =
        NorwegianCalendar.forNoon(foedselsdato).apply {
            add(Calendar.YEAR, age)
        }.let {
            getYear(it.time)
        }

    fun yearUserTurnsGivenAge(foedselsdato: LocalDate, age: Int): Int =
        yearUserTurnsGivenAge(foedselsdato.toNorwegianDateAtNoon(), age)

    // no.stelvio.common.util.DateUtil.getMonth
    fun getMonth(date: Date): Int =
        getField(date, Calendar.MONTH)

    // no.stelvio.common.util.DateUtil.getYear
    fun getYear(date: Date): Int =
        getField(date, Calendar.YEAR)

    /**
     * Finner datoer X dager fram/tilbake i tid.
     */
    // no.stelvio.common.util.DateUtil.getRelativeDateByDays
    fun getRelativeDateByDays(date: Date, days: Int): Date =
        NorwegianCalendar.forNoon(date).apply {
            add(Calendar.DAY_OF_MONTH, days)
        }.time

    fun getRelativeDateByDays(date: LocalDate, days: Int): LocalDate =
        getRelativeDateByDays(date.toNorwegianDateAtNoon(), days).toNorwegianLocalDate()

    /**
     * Finner datoer X måneder fremover/tilbake i tid.
     */
    fun getRelativeDateByMonth(date: Date, months: Int): Date =
        NorwegianCalendar.forNoon(date).apply {
            add(Calendar.MONTH, months)
        }.time

    /**
     * Finner datoer X år fram/tilbake i tid.
     */
    // no.stelvio.common.util.DateUtil.getRelativeDateByYear
    fun getRelativeDateByYear(date: Date, years: Int): Date =
        NorwegianCalendar.forNoon(date).apply {
            add(Calendar.YEAR, years)
        }.time

    fun getRelativeDateByYear(date: LocalDate, years: Int): LocalDate =
        getRelativeDateByYear(date.toNorwegianDateAtNoon(), years).toNorwegianLocalDate()

    /**
     * Returns the given field for a date.
     * Be careful when calling this from inside another synchronized block as it could lead to a dealock.
     */
    // no.stelvio.common.util.DateUtil.getField
    private fun getField(date: Date, dateField: Int): Int =
        NorwegianCalendar.forNoon(date)[dateField]

    /**
     * Finds the lowest date of two dates by day, down to the granularity of days (not milliseconds, which is the default
     * behaviour in the standard API). If one date is null, the other will be returned. If both are null, null will be returned.
     */
    // no.stelvio.common.util.DateUtil.findEarliestDateByDay
    fun findEarliestDateByDay(first: Date?, second: Date?): Date? =
        when {
            first == null -> second
            second == null -> first
            else -> if (isBeforeByDay(first, second, allowSameDay = true)) first else second
        }

    /**
     * Finds the latest of two dates by day, down to the granularity of days (not milliseconds, which is the default behaviour
     * in the standard API). If one date is null, the other will be returned. If both are null, null will be returned.
     */
    // no.stelvio.common.util.DateUtil.findLatestDateByDay
    fun findLatestDateByDay(first: Date?, second: Date?): Date? =
        when {
            first == null -> second
            second == null -> first
            else -> if (isAfterByDay(first, second, allowSameDay = true)) first else second
        }

    fun findLatestDateByDay(first: LocalDate?, second: LocalDate?): LocalDate? =
        findLatestDateByDay(first?.toNorwegianDateAtNoon(), second?.toNorwegianDateAtNoon())?.toNorwegianLocalDate()

    /**
     * Comparing two dates down to the granularity of days (not milliseconds, which is the default
     * behaviour in the standard API). If any date argument is null, it gets assigned to year zero, reasonably far from our
     * time.
     * If allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
     * and day. If set to false, the method returns false on this condition
     */
    // no.stelvio.common.util.DateUtil.isBeforeByDay
    fun isBeforeByDay(thisDate: Date?, thatDate: Date?, allowSameDay: Boolean): Boolean =
        compareDates(thisDate, thatDate, allowSameDay, isAfter = false)

    // no.stelvio.common.util.DateUtil.isBeforeByDay
    fun isBeforeByDay(thisDate: LocalDate?, thatDate: Date?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = thisDate?.toNorwegianDateAtNoon(),
            thatDate, // compareDates applies noon
            allowSameDay,
            isAfter = false
        )

    // no.stelvio.common.util.DateUtil.isBeforeByDay
    fun isBeforeByDay(thisDate: LocalDate?, thatDate: LocalDate?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = thisDate?.toNorwegianDateAtNoon(),
            thatDate = thatDate?.toNorwegianDateAtNoon(),
            allowSameDay,
            isAfter = false
        )

    fun isBeforeByDay(thisDate: Date?, thatDate: LocalDate?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate, // compareDates applies noon
            thatDate = thatDate?.toNorwegianDateAtNoon(),
            allowSameDay,
            isAfter = false
        )

    /**
     * Comparing two dates down to the granularity of days (not milliseconds, which is the default behaviour
     * in the standard API). If any date argument is null, it gets assigned to year zero, reasonably far from our time.
     * If allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
     * and day. If set to false, the method returns false on this condition
     */
    fun isAfterByDay(thisDate: Date?, thatDate: Date?, allowSameDay: Boolean): Boolean =
        compareDates(thisDate, thatDate, allowSameDay, isAfter = true)

    fun isAfterByDay(thisDate: LocalDate?, thatDate: Date?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = thisDate?.toNorwegianDateAtNoon(),
            thatDate, // compareDates applies noon
            allowSameDay,
            isAfter = true
        )

    fun isAfterByDay(thisDate: LocalDate?, thatDate: LocalDate?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = thisDate?.toNorwegianDateAtNoon(),
            thatDate = thatDate?.toNorwegianDateAtNoon(),
            allowSameDay,
            isAfter = true
        )

    fun isFirstDayOfMonth(date: LocalDate): Boolean =
        isFirstDayOfMonth(date.toNorwegianDateAtNoon())

    fun isFirstDayOfMonth(date: Date): Boolean {
        val calendar: Calendar = NorwegianCalendar.forNoon(date)
        return calendar.getActualMinimum(Calendar.DAY_OF_MONTH) == calendar[Calendar.DAY_OF_MONTH]
    }

    private fun lastDayOfMonth(date: LocalDate): LocalDate =
        date.plusMonths(1L).withDayOfMonth(1).minusDays(1L)

    /**
     * Finds the last day in the month before the given date.
     */
    // no.nav.domain.pensjon.common.util.DateUtils.findLastDayInMonthBefore
    private fun findLastDayInMonthBefore(date: Date): Date =
        NorwegianCalendar.forNoon(date).apply {
            this[Calendar.DAY_OF_MONTH] = getActualMinimum(Calendar.DAY_OF_MONTH)
            add(Calendar.DAY_OF_MONTH, -1)
        }.time

    /**
     * Comparing two dates down to the granularity of days (not milliseconds, which is the default
     * behaviour in the standard API).
     * If allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
     * and day. If set to false, the method returns false on this condition
     */
    // no.stelvio.common.util.DateUtil.compareDates
    private fun compareDates(thisDate: Date?, thatDate: Date?, allowSameDay: Boolean, isAfter: Boolean): Boolean {
        val thisCalendar: Calendar = createDayCalendar(thisDate)
        val thatCalendar: Calendar = createDayCalendar(thatDate)

        return when {
            allowSameDay && thisCalendar == thatCalendar -> true
            isAfter -> thisCalendar.after(thatCalendar)
            else -> thisCalendar.before(thatCalendar)
        }
    }

    /**
     * Create a calendar with only year, month and day set. If the passed date is null, it gets assigned to year zero,
     * reasonably far from our time.
     */
    // no.stelvio.common.util.DateUtil.createDayCalendar
    private fun createDayCalendar(date: Date?): Calendar =
        date?.let(NorwegianCalendar::forNoon)
            ?: NorwegianCalendar.instance().apply {
                clear()
                this[0, Calendar.JANUARY, 0, 0, 0] = 0
            }
}
