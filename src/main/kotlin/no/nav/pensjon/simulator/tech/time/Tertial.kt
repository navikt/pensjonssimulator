package no.nav.pensjon.simulator.tech.time

object Tertial {
    val kalendermaanederPerTertial: Map<Int, List<Kalendermaaned>> = mapOf(
        1 to listOf(
            Kalendermaaned.januar,
            Kalendermaaned.februar,
            Kalendermaaned.mars,
            Kalendermaaned.april
        ),
        2 to listOf(
            Kalendermaaned.mai,
            Kalendermaaned.juni,
            Kalendermaaned.juli,
            Kalendermaaned.august
        ),
        3 to listOf(
            Kalendermaaned.september,
            Kalendermaaned.oktober,
            Kalendermaaned.november,
            Kalendermaaned.desember
        )
    )

    fun kalendermaaneder(tertial: Int): List<Kalendermaaned> =
        kalendermaanederPerTertial.getValue(tertial)
}