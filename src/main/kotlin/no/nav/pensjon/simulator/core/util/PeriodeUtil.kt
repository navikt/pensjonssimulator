package no.nav.pensjon.simulator.core.util

import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpVedtak
import no.nav.pensjon.simulator.core.domain.Vedtak
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.tech.time.DateUtil.maaneder
import java.time.LocalDate
import java.util.*

// no.nav.domain.pensjon.common.PeriodisertInformasjonListeUtils
object PeriodeUtil {

    // Extract from no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettOutputHelper.getBelop
    /**
     * Number of months that overlap between the two periods [start1, end1] and [start2, end2]
     */
    fun numberOfMonths(
        start1: LocalDate, end1: LocalDate,
        start2: LocalDate, end2: LocalDate
    ): Int {
        val latestStart: LocalDate = start2.coerceAtLeast(start1)
        val earliestEnd: LocalDate = end2.coerceAtMost(end1)

        val adjustedEnd = earliestEnd.let {
            if (isLastDayOfMonth(it)) it.plusDays(1) else it
        }

        return maaneder(latestStart, adjustedEnd)
    }

    // no.nav.domain.pensjon.common.ArligInformasjonListeUtils.findValidForYear
    fun findValidForYear(list: List<VeietSatsResultat>?, year: Int): VeietSatsResultat? {
        if (list == null) return null

        var result: VeietSatsResultat? = null

        for (element in list) {
            if (element.ar.equals(year)) {
                result = element
                break
            }
        }

        return result
    }

    // PeriodisertInformasjonListeUtils.findValidForDate
    fun findValidForDate(list: List<AbstraktBeregningsResultat>, date: Date): AbstraktBeregningsResultat? =
        list.firstOrNull { isValidForDate(it, date) }

    fun findValidForDate(list: List<AbstraktBeregningsResultat>, date: LocalDate): AbstraktBeregningsResultat? =
        list.firstOrNull { isValidForDate(it, date.toNorwegianDateAtNoon()) }

    // PeriodisertInformasjonListeUtils.findValidForDate
    fun findValidForDate(list: List<Pensjonsbeholdning>, date: Date): Pensjonsbeholdning? =
        list.firstOrNull { isValidForDate(it, date) }

    // PeriodisertInformasjonListeUtils.findAllValidForDate
    fun findAllValidForDate(list: List<VilkarsVedtak>, date: Date): List<VilkarsVedtak> =
        list.filter { isValidForDate(it, date) }

    // PeriodisertInformasjonListeUtils.findEarliest
    fun findEarliest(list: List<AbstraktBeregningsResultat>): AbstraktBeregningsResultat? =
        when {
            list.isEmpty() -> null
            list.size == 1 -> list[0]
            else -> earliestAmong(list)
        }

    // PeriodisertInformasjonListeUtils.findEarliest
    fun findEarliest(list: List<Vedtak>): Vedtak? =
        when {
            list.isEmpty() -> null
            list.size == 1 -> list[0]
            else -> earliestAmong(list)
        }

    // PeriodisertInformasjonListeUtils.findLatest
    fun findLatest(list: List<AbstraktBeregningsResultat>): AbstraktBeregningsResultat? =
        when {
            list.isEmpty() -> null
            list.size == 1 -> list[0]
            else -> latestAmong(list)
        }

    // PeriodisertInformasjonListeUtils.findLatest
    fun findLatest(list: List<Pre2025OffentligAfpVedtak>): Pre2025OffentligAfpVedtak? =
        when {
            list.isEmpty() -> null
            list.size == 1 -> list[0]
            else -> latestAmong(list)
        }

    // PeriodisertInformasjonListeUtils.findLatest
    fun findLatest(list: List<Pensjonsbeholdning>): Pensjonsbeholdning? =
        when {
            list.isEmpty() -> null
            list.size == 1 -> list[0]
            else -> latestAmong(list)
        }

    // PeriodisertInformasjonListeUtils.findLatest
    fun findLatest(list: List<Vedtak>): Vedtak? =
        when {
            list.isEmpty() -> null
            list.size == 1 -> list[0]
            else -> latestAmong(list)
        }

    private fun isLastDayOfMonth(date: LocalDate): Boolean =
        date.plusDays(1L).dayOfMonth == 1

    // PeriodisertInformasjonUtils.isValidForDate
    private fun isValidForDate(tidsbegrenset: AbstraktBeregningsResultat, date: Date): Boolean =
        isDateInPeriod(date, tidsbegrenset.virkFom, tidsbegrenset.virkTom)

    // PeriodisertInformasjonUtils.isValidForDate
    private fun isValidForDate(tidsbegrenset: Pensjonsbeholdning, date: Date): Boolean =
        isDateInPeriod(date, tidsbegrenset.fom, tidsbegrenset.tom)

    // Part of PeriodisertInformasjonListeUtils.findAllValidForDate
    private fun isValidForDate(tidsbegrenset: VilkarsVedtak, date: Date): Boolean =
        isDateInPeriod(date, tidsbegrenset.virkFom, tidsbegrenset.virkTom)

    // Extracted from PeriodisertInformasjonListeUtils.findEarliest
    private fun earliestAmong(list: List<AbstraktBeregningsResultat>): AbstraktBeregningsResultat? {
        var result: AbstraktBeregningsResultat? = null
        var earliestFom = ETERNITY

        list.forEach {
            if (isBeforeByDay(it.virkFom, earliestFom, false)) {
                result = it
                earliestFom = it.virkFom!!
            }
        }

        return result
    }

    // Extracted from PeriodisertInformasjonListeUtils.findEarliest
    private fun earliestAmong(list: List<Vedtak>): Vedtak? {
        var result: Vedtak? = null
        var earliestFom = ETERNITY

        list.forEach {
            if (isBeforeByDay(it.fom, earliestFom, false)) {
                result = it
                earliestFom = it.fom.toNorwegianDateAtNoon()
            }
        }

        return result
    }

    // Extracted from PeriodisertInformasjonListeUtils.findLatest
    private fun latestAmong(list: List<AbstraktBeregningsResultat>): AbstraktBeregningsResultat? {
        var result: AbstraktBeregningsResultat? = null
        var latestFom: Date? = null

        list.forEach {
            if (isAfterByDay(it.virkFom, latestFom, false)) {
                result = it
                latestFom = it.virkFom
            }
        }

        return result
    }

    // Extracted from PeriodisertInformasjonListeUtils.findLatest
    private fun latestAmong(list: List<Pre2025OffentligAfpVedtak>): Pre2025OffentligAfpVedtak? {
        var result: Pre2025OffentligAfpVedtak? = null
        var latestFom: Date? = null

        list.forEach {
            if (isAfterByDay(it.fom, latestFom, false)) {
                result = it
                latestFom = it.fom.toNorwegianDateAtNoon()
            }
        }

        return result
    }

    // Extracted from PeriodisertInformasjonListeUtils.findLatest
    private fun latestAmong(list: List<Vedtak>): Vedtak? {
        var result: Vedtak? = null
        var latestFom: Date? = null

        list.forEach {
            if (isAfterByDay(it.fom, latestFom, false)) {
                result = it
                latestFom = it.fom.toNorwegianDateAtNoon()
            }
        }

        return result
    }

    // Extracted from PeriodisertInformasjonListeUtils.findLatest
    private fun latestAmong(list: List<Pensjonsbeholdning>): Pensjonsbeholdning? {
        var result: Pensjonsbeholdning? = null
        var latestFom: Date? = null

        list.forEach {
            if (isAfterByDay(it.fom, latestFom, false)) {
                result = it
                latestFom = it.fom
            }
        }

        return result
    }
}
