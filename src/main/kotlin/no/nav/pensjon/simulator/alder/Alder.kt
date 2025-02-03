package no.nav.pensjon.simulator.alder

import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate

data class Alder(val aar: Int, val maaneder: Int) {

    init {
        require(aar in 0..200) { "0 <= aar <= 200" }
        require(maaneder in 0..11) { "0 <= maaneder <= 11" }
    }

    infix fun lessThan(other: Alder?): Boolean =
        other?.let { aar < it.aar || aar == it.aar && maaneder < it.maaneder } != false

    infix fun plussMaaneder(antallMaaneder: Int): Alder =
        normalisedAlder(aar, maaneder = maaneder + antallMaaneder)

    fun antallMaanederEtter(alder: Alder): Int =
        MAANEDER_PER_AAR * (aar - alder.aar) + maaneder - alder.maaneder

    fun minusAar(antall: Int) = Alder(aar - antall, maaneder)

    fun plusAar(antall: Int) = Alder(aar + antall, maaneder)

    fun minusMaaneder(antall: Int): Alder {
        if (antall < 0) {
            return plusMaaneder(-antall)
        }

        val alderMaaneder = maaneder - antall % MAANEDER_PER_AAR
        val alderAar = aar - antall / MAANEDER_PER_AAR
        val underflow = alderMaaneder < 0

        return Alder(
            if (underflow) alderAar - 1 else alderAar,
            if (underflow) alderMaaneder + MAANEDER_PER_AAR else alderMaaneder
        )
    }

    fun plusMaaneder(antall: Int): Alder {
        if (antall < 0) {
            return minusMaaneder(-antall)
        }

        val alderMaaneder = maaneder + antall % MAANEDER_PER_AAR
        val alderAar = aar + antall / MAANEDER_PER_AAR
        val overflow = alderMaaneder >= MAANEDER_PER_AAR

        return Alder(
            if (overflow) alderAar + 1 else alderAar,
            if (overflow) alderMaaneder - MAANEDER_PER_AAR else alderMaaneder
        )
    }

    companion object {

        /**
         * Beregner alder ved angitt dato.
         * Kun helt fylte år og helt fylte måneder telles med.
         * (Eksempel: En alder av 5 år, 11 måneder og 27 dager returneres som 5 år og 11 måneder.)
         * Bakgrunnen for dette er at det i pensjonssammenheng opereres med hele måneder;
         * det er den første dag i påfølgende måned som legges til grunn ved f.eks. uttak av pensjon.
         */
        fun from(foedselsdato: LocalDate, dato: LocalDate): Alder {
            //val dagFratrekk = if (dato.dayOfMonth == 1 && foedselsdato.dayOfMonth == 1) 1 else 0
            val delmaanedFratrekk = if (dato.dayOfMonth - foedselsdato.dayOfMonth <= 0) 1 else 0
            // NB: Samme dayOfMonth anses som ufullstending måned og dermed fratrekk

            return normalisedAlder(
                aar = dato.year - foedselsdato.year,
                maaneder = dato.monthValue - foedselsdato.monthValue - delmaanedFratrekk
            )
        }

        /**
         * Normalised alder = alder with måneder within [0, 11]
         */
        private fun normalisedAlder(aar: Int, maaneder: Int): Alder =
            when (numberFlow(maaneder)) {
                NumberFlow.OVER -> Alder(
                    aar = aar + 1,
                    maaneder = maaneder - MAANEDER_PER_AAR
                )

                NumberFlow.UNDER -> Alder(
                    aar = aar - 1,
                    maaneder = maaneder + MAANEDER_PER_AAR
                )

                else -> Alder(aar, maaneder)
            }

        private fun numberFlow(maaneder: Int): NumberFlow =
            when {
                maaneder < 0 -> NumberFlow.UNDER
                maaneder >= MAANEDER_PER_AAR -> NumberFlow.OVER
                else -> NumberFlow.NEUTRAL
            }

        private enum class NumberFlow {
            UNDER,
            NEUTRAL,
            OVER
        }
    }
}
