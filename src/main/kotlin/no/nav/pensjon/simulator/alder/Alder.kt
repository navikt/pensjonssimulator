package no.nav.pensjon.simulator.alder

import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate

data class Alder(val aar: Int, val maaneder: Int) {

    init {
        require(aar in 0..200) { "0 <= aar <= 200" }
        require(maaneder in 0..11) { "0 <= maaneder <= 11" }
    }

    infix fun greaterThan(other: Alder?): Boolean =
        other?.let { aar > it.aar || aar == it.aar && maaneder > it.maaneder } != false

    infix fun lessThan(other: Alder?): Boolean =
        other?.let { aar < it.aar || aar == it.aar && maaneder < it.maaneder } != false

    infix fun plussMaaneder(antallMaaneder: Int): Alder =
        normalisedAlder(aar, maaneder = maaneder + antallMaaneder)

    fun antallMaanederEtter(alder: Alder): Int =
        MAANEDER_PER_AAR * (aar - alder.aar) + maaneder - alder.maaneder

    /**
     * Datoen da personen oppnår alderen.
     * Eksempel: En person født 15.6.2000 oppnår alderen 50 år 3 måneder på datoen 15.9.2050.
     */
    fun oppnaasDato(foedselsdato: LocalDate): LocalDate =
        foedselsdato.plusYears(aar.toLong()).plusMonths(maaneder.toLong())

    fun minusAar(antall: Int) = Alder(aar - antall, maaneder)

    infix fun plussAar(antall: Int) =
        Alder(aar + antall, maaneder)

    fun minusMaaneder(antall: Int): Alder {
        if (antall < 0) return plusMaaneder(-antall)

        val alderMaaneder = maaneder - antall % MAANEDER_PER_AAR
        val alderAar = aar - antall / MAANEDER_PER_AAR

        return if (alderMaaneder < 0)
            Alder(aar = alderAar - 1, maaneder = alderMaaneder + MAANEDER_PER_AAR)
        else
            Alder(aar = alderAar, maaneder = alderMaaneder)
    }

    fun plusMaaneder(antall: Int): Alder {
        if (antall < 0) return minusMaaneder(-antall)

        val alderMaaneder = maaneder + antall % MAANEDER_PER_AAR
        val alderAar = aar + antall / MAANEDER_PER_AAR

        return if (alderMaaneder >= MAANEDER_PER_AAR)
            Alder(aar = alderAar + 1, maaneder = alderMaaneder - MAANEDER_PER_AAR)
        else
            Alder(aar = alderAar, maaneder = alderMaaneder)
    }

    override fun toString(): String =
        when (maaneder) {
            0 -> "$aar år"
            1 -> "$aar år 1 måned"
            else -> "$aar år $maaneder måneder"
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
            val delmaanedFratrekk = if (dato.dayOfMonth - foedselsdato.dayOfMonth <= 0) 1 else 0
            // NB: Samme dayOfMonth anses som ufullstending måned og dermed fratrekk

            return normalisedAlder(
                aar = dato.year - foedselsdato.year,
                maaneder = dato.monthValue - foedselsdato.monthValue - delmaanedFratrekk
            )
        }

        /**
         * Normalised alder = alder with måneder within [0, 11].
         * NB: Not to be confused with "normert alder".
         */
        private fun normalisedAlder(aar: Int, maaneder: Int): Alder =
            when {
                maaneder < 0 -> Alder(
                    aar = aar - 1,
                    maaneder = maaneder + MAANEDER_PER_AAR
                )

                maaneder >= MAANEDER_PER_AAR -> Alder(
                    aar = aar + 1,
                    maaneder = maaneder - MAANEDER_PER_AAR
                )

                else -> Alder(aar, maaneder)
            }
    }
}
