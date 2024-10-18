package no.nav.pensjon.simulator.generelt.organisasjon

data class Organisasjonsnummer(
    val value: String
) {
    override fun toString(): String = value
}
