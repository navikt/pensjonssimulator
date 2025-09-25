package no.nav.tjenestepensjon.simulering.v2025.afp.v1

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.AlderForDelingstall
import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate
import java.time.Period

object AlderForDelingstallBeregner {
    private val hoyesteAlderForDelingstall = Alder(70, 0)
    private val lavestMuligUttaksalder = 62

    fun bestemAldreForDelingstall(fodselsdato: LocalDate, uttaksdato: LocalDate): List<AlderForDelingstall> {

        val aarGammelVedUttak = uttaksdato.year - fodselsdato.year

        if (aarGammelVedUttak == lavestMuligUttaksalder) {
            val alderVedUttak = bestemAlderVedDato(fodselsdato, uttaksdato)

            val aarskifteTilBrukerenBlir63 = LocalDate.of(uttaksdato.year + 1, 1, 1)
            val alderVedAarskifteTil63 = bestemAlderVedDato(fodselsdato, aarskifteTilBrukerenBlir63)

            return listOf(AlderForDelingstall(alderVedUttak, uttaksdato), AlderForDelingstall(alderVedAarskifteTil63, aarskifteTilBrukerenBlir63))
        }
        if (aarGammelVedUttak >= hoyesteAlderForDelingstall.aar) {
            return listOf(AlderForDelingstall(hoyesteAlderForDelingstall, uttaksdato))
        }
        val ufullstendigMaanedFratrekk = if (uttaksdato.dayOfMonth - fodselsdato.dayOfMonth == 0) 1 else 0

        return listOf(AlderForDelingstall(
            bestemAlderVedDato(fodselsdato, uttaksdato.minusMonths(ufullstendigMaanedFratrekk.toLong())),
            uttaksdato)
        )
    }

    private fun bestemAlderVedDato(fodselsdato: LocalDate, date: LocalDate): Alder {
        val periode = Period.between(fodselsdato, date)
        return Alder(periode.years, periode.months)
    }

}

