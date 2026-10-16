package no.nav.pensjon.simulator.core.spec

/**
 * Spesifiserer hvordan 'faktisk beholdning' (dvs. den som hentes fra opptjeningsregisteret) skal brukes.
 *
 * - 'settFaktiskBeholdningSomBeholdning' styrer om 'beholdning'-feltet skal settes til faktisk beholdning
 *
 * - 'resultatSkalInkludereFaktiskBeholdning' styrer om simuleringsresultatet skal innholde faktisk beholdning
 *   (og beregnet beholdning bare for årene der faktisk beholdning ike finnes)
 */
data class FaktiskBeholdningsbrukSpec(
    val settFaktiskBeholdningSomBeholdning: Boolean = false,
    val resultatSkalInkludereFaktiskBeholdning: Boolean = false
) {
    val brukBeholdning: Boolean = settFaktiskBeholdningSomBeholdning || resultatSkalInkludereFaktiskBeholdning
}