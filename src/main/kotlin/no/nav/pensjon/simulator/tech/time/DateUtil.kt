package no.nav.pensjon.simulator.tech.time

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object DateUtil {

    const val MAANEDER_PER_AAR = 12
    private const val DAGER_I_AARETS_SISTE_MAANED = 31
    private const val TIME_ZONE_ID = "Europe/Oslo"
    private val TIDENS_BEGYNNELSE: LocalDate = LocalDate.of(0, 1, 1)
    val TIDENS_SLUTT: LocalDate = LocalDate.of(9999, 12, 31)

    fun toLocalDate(dateTime: ZonedDateTime): LocalDate =
        dateTime.withZoneSameInstant(ZoneId.of(TIME_ZONE_ID)).toLocalDate()

    fun foersteDag(aar: Int): LocalDate =
        LocalDate.of(aar, 1, 1)

    fun sisteDag(aar: Int): LocalDate =
        LocalDate.of(aar, MAANEDER_PER_AAR, DAGER_I_AARETS_SISTE_MAANED)

    fun foersteDagNesteMaaned(dato: LocalDate): LocalDate =
        dato
            .plusMonths(1)
            .withDayOfMonth(1)

    /**
     * Finner antall hele måneder i perioden f.o.m.-t.o.m. som befinner seg innenfor et gitt år.
     * T.o.m.-datoen kan være udefinert (dvs. periode uten slutt).
     */
     fun maanederInnenforAaret(fom: LocalDate, tom: LocalDate?, aar: Int): Int =
        maanederInnenforRestenAvAaret(fom, tom, foersteDagAv(aar))

    /**
     * Finner antall hele måneder i perioden f.o.m.-t.o.m. som overlapper med perioden fra en gitt dato til siste dag i det samme året.
     * T.o.m.-datoen kan være udefinert (dvs. periode uten slutt).
     */
     fun maanederInnenforRestenAvAaret(fom: LocalDate, nullableTom: LocalDate?, start: LocalDate): Int {
        val aaretsSisteDag = sisteDagAv(start.year)
        val tom = nullableTom ?: aaretsSisteDag
        if (start.year < fom.year || start.isAfter(tom)) return 0

        val periodisertFom: LocalDate = start.let { if (it.isBefore(fom)) fom else it }
        val periodisertTom: LocalDate = aaretsSisteDag.let { if (tom.isBefore(it)) tom else it }

        return maaneder(
            fom = periodisertFom,
            til = periodisertTom.plusDays(1) // til (men ikke med) = dagen etter til-og-med
        )
    }

    /**
     * Finner antall hele måneder i perioden fra og med (fom) en gitt dato til (men ikke med) en annen gitt dato.
     */
    fun maaneder(fom: LocalDate, til: LocalDate): Int {
        val maanedDiff: Int = MAANEDER_PER_AAR * (til.year - fom.year) + til.monthValue - fom.monthValue
        return if (fom.dayOfMonth > til.dayOfMonth) maanedDiff - 1 else maanedDiff // delvis måned teller ikke med
    }

    /**
     * PEN: no.stelvio.common.util.DateUtil.intersectsWithPossiblyOpenEndings
     * NB: Here TIDENS_SLUTT is used instead of LocalDate MAX, to avoid timezone problems.
     */
    fun overlapperEndeloest(
        start1: LocalDate?, slutt1: LocalDate?,
        start2: LocalDate?, slutt2: LocalDate?,
        anseEnkeltDagSomOverlapp: Boolean
    ): Boolean =
        overlapper(
            start1,
            slutt1 ?: TIDENS_SLUTT,
            start2,
            slutt2 ?: TIDENS_SLUTT,
            anseEnkeltDagSomOverlapp
        )

    /**
     * PEN: no.stelvio.common.util.DateUtil.intersects
     */
    fun overlapper(
        start1: LocalDate?, slutt1: LocalDate?,
        start2: LocalDate?, slutt2: LocalDate?,
        anseEnkeltDagSomOverlapp: Boolean
    ): Boolean =
        intersects(
            start1 = start1 ?: TIDENS_BEGYNNELSE,
            end1 = slutt1 ?: TIDENS_BEGYNNELSE, // NB: Default er tidens begynnelse, ikke slutt
            start2 = start2 ?: TIDENS_BEGYNNELSE,
            end2 = slutt2 ?: TIDENS_BEGYNNELSE, // NB: Default er tidens begynnelse, ikke slutt
            considerContactByDayAsIntersection = anseEnkeltDagSomOverlapp
        )

    private fun foersteDagAv(aar: Int) =
        LocalDate.of(aar, 1, 1)

    private fun sisteDagAv(aar: Int) =
        LocalDate.of(aar, 12, 31)

    private fun intersects(
        start1: LocalDate, end1: LocalDate,
        start2: LocalDate, end2: LocalDate,
        considerContactByDayAsIntersection: Boolean
    ): Boolean {
        val isPoint = start1.isEqual(end1) || start2.isEqual(end2)
        val latestStart = if (start1.isAfter(start2)) start1 else start2
        val earliestEnd = if (end1.isBefore(end2)) end1 else end2

        return if (considerContactByDayAsIntersection || isPoint)
            latestStart.isAfter(earliestEnd).not()
        else
            latestStart.isBefore(earliestEnd)
    }
}

