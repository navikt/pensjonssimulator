package no.nav.pensjon.simulator.tech.time

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object DateUtil {

    const val MAANEDER_PER_AAR = 12
    private const val DAGER_I_AARETS_SISTE_MAANED = 31
    private const val TIME_ZONE_ID = "Europe/Oslo"

    fun toLocalDate(dateTime: ZonedDateTime): LocalDate =
        dateTime.withZoneSameInstant(ZoneId.of(TIME_ZONE_ID)).toLocalDate()

    fun foersteDag(aar: Int) =
        LocalDate.of(aar, 1, 1)

    fun sisteDag(aar: Int) =
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

    private fun foersteDagAv(aar: Int) = LocalDate.of(aar, 1, 1)

    private fun sisteDagAv(aar: Int) = LocalDate.of(aar, 12, 31)
}

