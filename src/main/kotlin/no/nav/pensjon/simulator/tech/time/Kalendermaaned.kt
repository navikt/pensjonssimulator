package no.nav.pensjon.simulator.tech.time

import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR

data class Kalendermaaned(
    val nummer: Int, // 1..12
    val navn: String
) {
    val erAaretsSiste: Boolean = nummer == MAANEDER_PER_AAR

    fun neste(): Kalendermaaned =
        fromInt(maanedsnummer = if (erAaretsSiste) 1 else nummer + 1)

    companion object {
        val januar = Kalendermaaned(1, "januar")
        val februar = Kalendermaaned(2, "februar")
        val mars = Kalendermaaned(3, "mars")
        val april = Kalendermaaned(4, "april")
        val mai = Kalendermaaned(5, "mai")
        val juni = Kalendermaaned(6, "juni")
        val juli = Kalendermaaned(7, "juli")
        val august = Kalendermaaned(8, "august")
        val september = Kalendermaaned(9, "september")
        val oktober = Kalendermaaned(10, "oktober")
        val november = Kalendermaaned(11, "november")
        val desember = Kalendermaaned(12, "desember")

        fun fromInt(maanedsnummer: Int): Kalendermaaned =
            when (maanedsnummer) {
                1 -> januar
                2 -> februar
                3 -> mars
                4 -> april
                5 -> mai
                6 -> juni
                7 -> juli
                8 -> august
                9 -> september
                10 -> oktober
                11 -> november
                12 -> desember
                else -> throw IllegalArgumentException("Ugyldig månedsnummer: $maanedsnummer - må være 1..12")
            }
    }
}