package no.nav.pensjon.simulator.tech.time

data class AarMaaned(
    val aar: Int,
    val maaned: Kalendermaaned
) {
    val asInt: Int = toInt(aar, maaned.nummer)

    fun neste() =
        AarMaaned(
            aar = if (maaned.erAaretsSiste) aar + 1 else aar,
            maaned = maaned.neste()
        )

    infix fun erEtter(other: AarMaaned) =
        asInt > other.asInt

    companion object {
        fun from(aar: Int, maaned: Int) =
            AarMaaned(aar, maaned = Kalendermaaned.fromInt(maanedsnummer = maaned))

        fun toInt(aar: Int, maaned: Int) =
            aar * 100 + maaned
    }
}