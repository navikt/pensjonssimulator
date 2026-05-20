package no.nav.pensjon.simulator.statistikk

data class AntallCombo(
    val navAntall: Int,
    val totaltAntall: Int
) {
    infix fun minus(other: AntallCombo) =
        AntallCombo(
            navAntall = this.navAntall - other.navAntall,
            totaltAntall = this.totaltAntall - other.totaltAntall
        )
}