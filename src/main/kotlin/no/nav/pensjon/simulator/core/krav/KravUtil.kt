package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.tech.time.DateUtil.maanederInnenforAaret
import no.nav.pensjon.simulator.tech.time.DateUtil.maanederInnenforRestenAvAaret
import java.time.LocalDate

object KravUtil {

    /**
     * Finner antall hele måneder utenlands i et gitt år.
     */
    fun utlandMaanederInnenforAaret(spec: SimuleringSpec, year: Int): Int =
        spec.utlandPeriodeListe.maxOfOrNull {
            maanederInnenforAaret(it.fom, it.tom, year)
        } ?: 0

    /**
     * Finner antall hele måneder utenlands i perioden fra første uttaksdato til siste dag i det samme året.
     */
    fun utlandMaanederInnenforRestenAvAaret(spec: SimuleringSpec): Int =
        spec.utlandPeriodeListe.maxOfOrNull {
            maanederInnenforRestenAvAaret(it.fom, it.tom, spec.foersteUttakDato!!)
        } ?: 0

    /**
     * Finner antall hele måneder utenlands i perioden fra første dag i uttaksåret til første uttaksdato.
     */
    fun utlandMaanederFraAarStartTilFoersteUttakDato(spec: SimuleringSpec): Int =
        spec.utlandPeriodeListe.maxOfOrNull {
            maanederInnenforAarStartTilDato(it.fom, it.tom, spec.foersteUttakDato!!)
        } ?: 0

    /**
     * Gitt en dato (dd.mm.yyyy), returneres antall hele måneder i perioden f.o.m.-t.o.m. som overlapper med perioden 01.01.yyyy-dd.mm.yyyy.
     * T.o.m.-datoen kan være udefinert (dvs. periode uten slutt).
     */
    private fun maanederInnenforAarStartTilDato(fom: LocalDate, nullableTom: LocalDate?, dato: LocalDate): Int {
        val aaretsFoersteDag = foersteDagAv(dato.year)
        val tom = nullableTom ?: sisteDagAv(dato.year)
        if (tom.year < dato.year || fom.isAfter(dato)) return 0

        val periodisertFom: LocalDate = aaretsFoersteDag.let { if (fom.isBefore(it)) it else fom }
        val periodisertTom: LocalDate = dato.let { if (it.isBefore(tom)) it else tom }

        return maaneder(
            fom = periodisertFom,
            til = periodisertTom.plusDays(1) // til (men ikke med) = dagen etter til-og-med
        )
    }

    /**
     * Finner antall hele måneder i perioden fra og med (fom) en gitt dato til (men ikke med) en annen gitt dato.
     */
    private fun maaneder(fom: LocalDate, til: LocalDate): Int {
        val maanedDiff: Int = MAANEDER_PER_AAR * (til.year - fom.year) + til.monthValue - fom.monthValue
        return if (fom.dayOfMonth > til.dayOfMonth) maanedDiff - 1 else maanedDiff // delvis måned teller ikke med
    }

    private fun foersteDagAv(aar: Int) = LocalDate.of(aar, 1, 1)

    private fun sisteDagAv(aar: Int) = LocalDate.of(aar, 12, 31)
}

