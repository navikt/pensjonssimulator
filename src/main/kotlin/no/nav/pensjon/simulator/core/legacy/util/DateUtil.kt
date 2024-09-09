package no.nav.pensjon.simulator.core.legacy.util

import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object DateUtil {

    val LOCAL_ETERNITY: LocalDate = LocalDate.of(9999, 12, 31)
    val ETERNITY: Date = fromLocalDate(LOCAL_ETERNITY)!!
    const val MAANEDER_PER_AR = 12

    // SimuleringEtter2011Utils.monthOfYearRange1To12
    fun monthOfYearRange1To12(date: Date?): Int =
        getMonth(date) + 1

    fun monthOfYearRange1To12(date: LocalDate?): Int =
        monthOfYearRange1To12(fromLocalDate(date))

    /**
     * {@see DateUtil.intersects} Removes the values for HOUR_OF_DAY, MINUTES, SECONDS and MILLISECONDS before the compare such
     * that same day is regarded as intersection if `considerContactByDayAsIntersection` is true. The endings of
     * the respective periods can be NULL, if so these will be set to infinity.
     */
    fun intersectsWithPossiblyOpenEndings(
        o1Start: LocalDate?, o1End: LocalDate?, o2Start: LocalDate?, o2End: LocalDate?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean =
        intersectsWithPossiblyOpenEndings(
            fromLocalDate(o1Start),
            fromLocalDate(o1End),
            fromLocalDate(o2Start),
            fromLocalDate(o2End),
            considerContactByDayAsIntersection
        )

    fun intersectsWithPossiblyOpenEndings(
        o1Start: LocalDate?, o1End: LocalDate?, o2Start: Date?, o2End: Date?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean =
        intersectsWithPossiblyOpenEndings(
            fromLocalDate(o1Start),
            fromLocalDate(o1End),
            o2Start,
            o2End,
            considerContactByDayAsIntersection
        )

    fun intersectsWithPossiblyOpenEndings(
        o1Start: Date?, o1End: Date?, o2Start: Date?, o2End: Date?,
        considerContactByDayAsIntersection: Boolean
    ): Boolean =
        intersects(
            o1Start, if ((o1End == null)) Date(Long.MAX_VALUE) else o1End, o2Start,
            if ((o2End == null)) Date(Long.MAX_VALUE) else o2End, considerContactByDayAsIntersection
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

    /**
     * Calculates the last day of the month the user turns 67 years of age.
     */
    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011Utils.lastDayOfMonthUserTurns67
    fun lastDayOfMonthUserTurns67(foedselDato: Date?): Date =
        lastDayOfMonthUserTurnsGivenAge(foedselDato, 67)

    fun lastDayOfMonthUserTurns67(foedselDato: LocalDate?): LocalDate =
        local(lastDayOfMonthUserTurns67(fromLocalDate(foedselDato)))!!

    /**
     * Calculates the age in years of a person on a given date. The age is returned "NAV style", i.e. the user is considered to
     * be of a certain age in a 12-month period starting from the 1st of the month after the birthday month.
     */
    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011Utils.calculateAgeInYears
    fun calculateAgeInYears(foedselDato: Date, compareDate: Date): Int {
        /*
        val adjustedDateOfBirth: Date = getLastDayOfMonth(dateOfBirth)
        val period: Period = Period(adjustedDateOfBirth.time, compareDate.time, PeriodType.years())
        return period.getYears()
        */
        return calculateAgeInYears(local(foedselDato), local(compareDate))
    }

    fun calculateAgeInYears(foedselDato: Date, compareDate: LocalDate): Int =
        calculateAgeInYears(local(foedselDato), compareDate)

    // TODO compare this with SimuleringRequestConverter.convertDatoFomToAlder
    fun calculateAgeInYears(foedselDato: LocalDate?, dato: LocalDate?): Int =
        Period.between(
            foedselDato?.withDayOfMonth(1),
            dato?.withDayOfMonth(1)
        ).years

    /**
     * Calculates the last day of the month the user turns the given number of years of age.
     */
    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011Utils.lastDayOfMonthUserTurnsGivenAge
    private fun lastDayOfMonthUserTurnsGivenAge(foedselDato: Date?, age: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = foedselDato
        calendar.add(Calendar.YEAR, age)
        calendar.add(Calendar.MONTH, 1)
        return setTimeToZero(findLastDayInMonthBefore(calendar.time))
    }

    /**
     * Finds the last day in the month before the given date.
     */
    // no.nav.domain.pensjon.common.util.DateUtils.findLastDayInMonthBefore
    private fun findLastDayInMonthBefore(date: Date): Date {
        val calendar: Calendar = createCalendar(date)
        calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.time
    }

    // no.nav.domain.pensjon.common.util.DateUtils.fromLocalDate
    fun fromLocalDate(date: LocalDate?): Date? {
        if (date == null) {
            return null
        }

        return createCalendar(
            year = date.year,
            month = date.monthValue - 1,
            day = date.dayOfMonth
        ).time
    }

    fun setTimeToZero(date: Date): Date = createEmptyTimeFieldsCalendar(date).time

    /**
     * Creates a new Date object set to the date fields given by the input parameters. All other date fields are cleared.
     */
    fun createDate(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar[year, month] = day
        return calendar.time
    }

    /**
     * Returns the number of months between two dates.
     */
    // no.stelvio.common.util.DateUtil.getMonthBetween
    fun getMonthBetween(fromDate: Date?, toDate: Date?): Int {
        val fromCalendar: Calendar = createCalendar(fromDate)
        val toCalendar: Calendar = createCalendar(toDate)

        val fromTotalMonths: Int =
            MAANEDER_PER_AR * fromCalendar[Calendar.YEAR] + fromCalendar[Calendar.MONTH]
        val toTotalMonths: Int =
            MAANEDER_PER_AR * toCalendar[Calendar.YEAR] + toCalendar[Calendar.MONTH]

        return abs((fromTotalMonths - toTotalMonths).toDouble()).toInt()
    }

    fun getMonthBetween(fromDate: LocalDate, toDate: LocalDate): Int =
        getMonthBetween(fromLocalDate(fromDate), fromLocalDate(toDate))

    // no.stelvio.common.util.DateUtil.isDateInPeriod
    fun isDateInPeriod(compDate: Date?, fomDate: Date?, tomDate: Date?): Boolean {
        if (null == fomDate || null == compDate) {
            return false
        }

        var tomOK = false

        if (null != tomDate) {
            if (isBeforeDay(compDate, tomDate) || isSameDay(compDate, tomDate)) {
                tomOK = true
            }
        } else {
            tomOK = true
        }

        return (isBeforeDay(fomDate, compDate) || isSameDay(compDate, fomDate)) && tomOK
    }

    fun isDateInPeriod(compDate: LocalDate?, fomDate: Date?, tomDate: Date?): Boolean =
        isDateInPeriod(fromLocalDate(compDate), fomDate, tomDate)

    /**
     * Returns true if the dates are the same day.
     * Hours, minutes, seconds and milliseconds are not regarded.
     */
    // no.stelvio.common.util.DateUtil.isSameDay
    fun isSameDay(a: Date?, b: Date?): Boolean {
        if (a == null && b == null) {
            return false
        }

        val thisCal: Calendar = createDayCalendar(a)
        val thatCal: Calendar = createDayCalendar(b)
        return thisCal == thatCal
    }

    fun isSameDay(a: LocalDate?, b: Date?): Boolean =
        isSameDay(fromLocalDate(a), b)

    fun isSameDay(a: LocalDate?, b: LocalDate?): Boolean =
        isSameDay(fromLocalDate(a), fromLocalDate(b))

    /**
     * Checks if one date is before another date.
     * Only uses the date portion of the input, not taking the time portion in account.
     */
    // no.stelvio.common.util.DateUtil.isBeforeDay
    fun isBeforeDay(first: Date?, second: Date?): Boolean {
        if (first == null) {
            return false
        }

        val firstCalendar: Calendar = createEmptyTimeFieldsCalendar(first)
        val secondCalendar: Calendar = createEmptyTimeFieldsCalendar(second ?: Date())
        return firstCalendar.time.before(secondCalendar.time)
    }

    fun isBeforeDay(first: Date?, second: LocalDate?): Boolean =
        isBeforeDay(first, fromLocalDate(second))

    fun isBeforeDay(first: LocalDate?, second: LocalDate?): Boolean =
        isBeforeDay(fromLocalDate(first), fromLocalDate(second))

    // no.stelvio.common.util.DateUtil.isBeforeToday
    fun isBeforeToday(date: Date?): Boolean = isBeforeDay(date, null as Date?)

    // no.stelvio.common.util.DateUtil.getYesterday
    fun getYesterday(): Date = getRelativeDateFromNow(-1)

    // SimuleringEtter2011Utils.firstDayOfMonthAfterUserTurnsGivenAge
    fun firstDayOfMonthAfterUserTurnsGivenAge(dateOfBirth: Date?, age: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = dateOfBirth
        calendar.add(Calendar.YEAR, age)
        calendar.add(Calendar.MONTH, 1)
        calendar[Calendar.DAY_OF_MONTH] = 1
        return setTimeToZero(calendar.time)
    }

    fun firstDayOfMonthAfterUserTurnsGivenAge(dateOfBirth: LocalDate, age: Int): Date =
        firstDayOfMonthAfterUserTurnsGivenAge(fromLocalDate(dateOfBirth), age)

    // no.stelvio.common.util.DateUtil.getFirstDayOfMonth
    fun getFirstDayOfMonth(date: Date): Date =
        getFirstOrLastDayOfMonth(date, true)

    fun getFirstDayOfMonth(date: LocalDate): Date =
        getFirstOrLastDayOfMonth(fromLocalDate(date)!!, true)

    fun getFirstDayOfMonth2(date: LocalDate): LocalDate =
        local(getFirstOrLastDayOfMonth(fromLocalDate(date), true))!!

    // no.stelvio.common.util.DateUtil.getFirstOrLastDayOfMonth
    private fun getFirstOrLastDayOfMonth(date: Date?, first: Boolean): Date {
        val calendar: Calendar = createCalendar(date)
        val dayOfMonth =
            if (first) calendar.getActualMinimum(Calendar.DAY_OF_MONTH) else calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        return calendar.time
    }

    fun getLastDayOfMonth(date: Date?): Date =
        getFirstOrLastDayOfMonth(date, false)

    // no.stelvio.common.util.DateUtil.getFirstDateInYear
    fun getFirstDateInYear(date: Date?): Date {
        val calendar: Calendar = createEmptyTimeFieldsCalendar(date)
        calendar[Calendar.MONTH] = Calendar.JANUARY
        calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        return calendar.time
    }

    fun getFirstDateInYear(date: LocalDate?): LocalDate =
        local(getFirstDateInYear(fromLocalDate(date)))!!

    // no.stelvio.common.util.DateUtil.getLastDateInYear
    fun getLastDateInYear(date: Date?): Date {
        val calendar: Calendar = createEmptyTimeFieldsCalendar(date)
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return calendar.time
    }

    fun getLastDateInYear(date: LocalDate): LocalDate =
        local(getLastDateInYear(fromLocalDate(date)))!!

    // no.stelvio.common.util.DateUtil.getRelativeDateFromNow
    private fun getRelativeDateFromNow(days: Int): Date =
        getRelativeDateByDays(fromLocalDate(LocalDate.now()), days)

    // no.stelvio.common.util.DateUtil.createEmptyTimeFieldsCalendar
    private fun createEmptyTimeFieldsCalendar(date: Date?): Calendar {
        val calendar: Calendar = createCalendar(date)
        clearTimeFields(calendar)
        return calendar
    }

    // SimuleringEtter2011Utils.yearUserTurnsGivenAge
    fun yearUserTurnsGivenAge(foedselDato: Date?, age: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.time = foedselDato
        calendar.add(Calendar.YEAR, age)
        return getYear(calendar.time)
    }

    fun yearUserTurnsGivenAge(foedselDato: LocalDate, age: Int): Int =
        yearUserTurnsGivenAge(fromLocalDate(foedselDato), age)

    // no.stelvio.common.util.DateUtil.getMonth
    fun getMonth(date: Date?): Int =
        getField(date, Calendar.MONTH)

    // no.stelvio.common.util.DateUtil.getYear
    fun getYear(date: Date?): Int =
        getField(date, Calendar.YEAR)

    /**
     * Finner datoer X dager fram/tilbake i tid.
     */
    // no.stelvio.common.util.DateUtil.getRelativeDateByDays
    fun getRelativeDateByDays(date: Date?, days: Int): Date {
        val calendar: Calendar = createCalendar(date)
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }

    fun getRelativeDateByDays(date: LocalDate?, days: Int): LocalDate =
        local(getRelativeDateByDays(fromLocalDate(date), days))!!

    /**
     * Finner datoer X måneder fremover/tilbake i tid.
     */
    fun getRelativeDateByMonth(date: Date?, months: Int): Date {
        val calendar: Calendar = createCalendar(date)
        calendar.add(Calendar.MONTH, months)
        return calendar.time
    }

    /**
     * Finner datoer X år fram/tilbake i tid.
     */
    // no.stelvio.common.util.DateUtil.getRelativeDateByYear
    fun getRelativeDateByYear(date: Date?, years: Int): Date {
        val calendar: Calendar = createCalendar(date)
        calendar.add(Calendar.YEAR, years)
        return calendar.time
    }

    fun getRelativeDateByYear(date: LocalDate?, years: Int): LocalDate =
        local(getRelativeDateByYear(fromLocalDate(date), years))!!

    /**
     * Returns the given field for a date.
     * Be careful when calling this from inside another synchronized block as it could lead to a dealock.
     */
    // no.stelvio.common.util.DateUtil.getField
    private fun getField(date: Date?, dateField: Int): Int {
        val calendar: Calendar = createCalendar(date)
        return calendar[dateField]
    }

    /**
     * Finds the lowest date of two dates by day, down to the granularity of days (not milliseconds, which is the default
     * behaviour in the standard API). If one date is null, the other will be returned. If both are null, null will be returned.
     */
    // no.stelvio.common.util.DateUtil.findEarliestDateByDay
    fun findEarliestDateByDay(first: Date?, second: Date?): Date? {
        return if (first == null) {
            second
        } else if (second == null) {
            first
        } else {
            if (isBeforeByDay(first, second, true)) first else second
        }
    }

    /**
     * Finds the latest of two dates by day, down to the granularity of days (not milliseconds, which is the default behaviour
     * in the standard API). If one date is null, the other will be returned. If both are null, null will be returned.
     */
    // no.stelvio.common.util.DateUtil.findLatestDateByDay
    fun findLatestDateByDay(first: Date?, second: Date?): Date? =
        if (first == null) {
            second
        } else if (second == null) {
            first
        } else {
            if (isAfterByDay(first, second, true)) first else second
        }

    fun findLatestDateByDay(first: LocalDate?, second: LocalDate?): LocalDate? =
        local(findLatestDateByDay(fromLocalDate(first), fromLocalDate(second)))

    /**
     * Comparing two dates down to the granularity of days (not milliseconds, which is the default
     * behaviour in the standard API). If any date argument is null, it gets assigned to year zero, reasonably far from our
     * time.
     * If allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
     * and day. If set to false, the method returns false on this condition
     */
    // no.stelvio.common.util.DateUtil.isBeforeByDay
    fun isBeforeByDay(thisDate: Date?, thatDate: Date?, allowSameDay: Boolean): Boolean {
        return compareDates(thisDate, thatDate, allowSameDay, false)
    }

    // no.stelvio.common.util.DateUtil.isBeforeByDay
    fun isBeforeByDay(thisDate: LocalDate?, thatDate: Date?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = fromLocalDate(thisDate),
            thatDate = thatDate,
            allowSameDay = allowSameDay,
            isAfter = false
        )

    // no.stelvio.common.util.DateUtil.isBeforeByDay
    fun isBeforeByDay(thisDate: LocalDate?, thatDate: LocalDate?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = fromLocalDate(thisDate),
            thatDate = fromLocalDate(thatDate),
            allowSameDay = allowSameDay,
            isAfter = false
        )

    fun isBeforeByDay(thisDate: Date?, thatDate: LocalDate?, allowSameDay: Boolean): Boolean =
        compareDates(
            thisDate = thisDate,
            thatDate = fromLocalDate(thatDate),
            allowSameDay = allowSameDay,
            isAfter = false
        )

    /**
     * Comparing two dates down to the granularity of days (not milliseconds, which is the default behaviour
     * in the standard API). If any date argument is null, it gets assigned to year zero, reasonably far from our time.
     * If allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
     * and day. If set to false, the method returns false on this condition
     */
    fun isAfterByDay(thisDate: Date?, thatDate: Date?, allowSameDay: Boolean): Boolean {
        return compareDates(thisDate, thatDate, allowSameDay, true)
    }

    fun isAfterByDay(thisDate: LocalDate?, thatDate: Date?, allowSameDay: Boolean): Boolean {
        return compareDates(
            fromLocalDate(thisDate),
            thatDate,
            allowSameDay,
            true
        )
    }

    fun isAfterByDay(thisDate: LocalDate?, thatDate: LocalDate?, allowSameDay: Boolean): Boolean {
        return compareDates(
            fromLocalDate(thisDate),
            fromLocalDate(thatDate),
            allowSameDay,
            true
        )
    }

    fun isAfterToday(date: LocalDate?): Boolean = isAfterToday(fromLocalDate(date))

    fun isAfterToday(date: Date?): Boolean {
        if (date == null) {
            throw IllegalArgumentException("null is a not valid input date")
        }

        return !isBeforeToday(date) && !isToday(date)
    }

    fun isFirstDayOfMonth(date: LocalDate?): Boolean =
        isFirstDayOfMonth(fromLocalDate(date))

    fun isFirstDayOfMonth(date: Date?): Boolean {
        if (date == null) {
            throw IllegalArgumentException("null is a not valid input date")
        }

        val calendar: Calendar = createCalendar(date)
        return calendar.getActualMinimum(Calendar.DAY_OF_MONTH) == calendar[Calendar.DAY_OF_MONTH]
    }

    private fun isToday(date: Date?): Boolean {
        return isSameDay(date, fromLocalDate(LocalDate.now()))
    }

    /**
     * Comparing two dates down to the granularity of days (not milliseconds, which is the default
     * behaviour in the standard API).
     * If allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
     * and day. If set to false, the method returns false on this condition
     */
    // no.stelvio.common.util.DateUtil.compareDates
    private fun compareDates(thisDate: Date?, thatDate: Date?, allowSameDay: Boolean, isAfter: Boolean): Boolean {
        val thisCal: Calendar = createDayCalendar(thisDate)
        val thatCal: Calendar = createDayCalendar(thatDate)

        if (allowSameDay && thisCal == thatCal) {
            return true
        }
        return if (isAfter) {
            thisCal.after(thatCal)
        } else {
            thisCal.before(thatCal)
        }
    }

    /**
     * Create a calendar with only year, month and day set. If the passed date is null, it gets assigned to year zero,
     * reasonably far from our time.
     */
    // no.stelvio.common.util.DateUtil.createDayCalendar
    private fun createDayCalendar(date: Date?): Calendar {
        val cal = Calendar.getInstance()
        if (date == null) {
            cal.clear()
            cal[0, Calendar.JANUARY, 0, 0, 0] = 0
        } else {
            cal.time = date
            clearTimeFields(cal)
        }
        return cal
    }

    // no.stelvio.common.util.DateUtil.clearTimeFields
    private fun clearTimeFields(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }

    // no.stelvio.common.util.DateUtil.createCalendar
    private fun createCalendar(date: Date?): Calendar {
        val calendar = Calendar.getInstance()
        calendar.isLenient = false
        calendar.time = date
        return calendar
    }

    // no.nav.domain.pensjon.common.util.DateUtils.createCalendar
    private fun createCalendar(year: Int, month: Int, day: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar[year, month] = day
        return calendar
    }

    // no.stelvio.common.util.DateUtil.toLocalDate
    private fun local(date: Date?): LocalDate? {
        if (date == null) {
            return null
        }

        val calendar = Calendar.getInstance().apply { time = date }
        return LocalDate.of(calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH])
    }
}
