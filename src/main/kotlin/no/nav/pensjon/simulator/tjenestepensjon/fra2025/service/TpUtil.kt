package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Maanedsutbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import java.time.LocalDate
import java.time.Period

object TpUtil {
    private val FNR_REGEX = """[0-9]{11}""".toRegex()

    fun grupperMedDatoFra(utbetalingsliste: List<Utbetalingsperiode>, foedselsdato: LocalDate): List<Maanedsutbetaling> {
        return utbetalingsliste
            .groupBy { it.fom }
            .map { (datoFra, ytelser) ->
                val totalMaanedsBelop = ytelser.sumOf { it.maanedligBelop }
                Maanedsutbetaling(datoFra, bestemUttaksalderVedDato(foedselsdato, datoFra), totalMaanedsBelop)
            }
            .sortedBy { it.fraOgMedDato }
    }

    /*
     * Funksjonen returnerer uttaksaderen ved uttaksdato, kan være lavere enn faktisk alder, når, f eks,
     * bruker er født 1. i måned. Da skal brukeren ta ut pensjon en måned senere, selv om man er teknisk sett 1 måned eldre
     * 1.1.2001 -> 1.2.2063 = 62 år og 0 måneder
     */
    fun bestemUttaksalderVedDato(fodselsdato: LocalDate, date: LocalDate): Alder {
        val periode = Period.between(fodselsdato, date)
        if (fodselsdato.dayOfMonth == 1) {
            return if (periode.months - 1 < 0) { //Substraction of months doesn't affect years in java.time.Period
                Alder(periode.years - 1, 11)
            } else {
                Alder(periode.years, periode.months - 1)
            }
        }
        return Alder(periode.years, periode.months)
    }

    fun redact(string: String): String = string.replace(FNR_REGEX) { "***********" }
}