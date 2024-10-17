package no.nav.pensjon.simulator.alder

data class Alder(val aar: Int, val maaneder: Int) {

    init {
        require(aar in 0..200) { "0 <= aar <= 200" }
        require(maaneder in 0..11) { "0 <= maaneder <= 11" }
    }

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
        private const val MAANEDER_PER_AAR = 12
    }
}
