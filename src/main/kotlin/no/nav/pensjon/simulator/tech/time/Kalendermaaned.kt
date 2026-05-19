package no.nav.pensjon.simulator.tech.time

import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR

data class Kalendermaaned(
    val nummer: Int, // 1..12
    val navn: String
) {
    val forrigeNummer = if (nummer == 1) MAANEDER_PER_AAR else nummer - 1
}