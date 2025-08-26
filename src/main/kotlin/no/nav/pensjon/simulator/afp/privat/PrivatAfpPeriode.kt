package no.nav.pensjon.simulator.afp.privat

/**
 * Resultat av simulering av privat AFP for en gitt periode.
 * PEN: no.nav.domain.pensjon.kjerne.simulering.SimulertAfpPrivatperiode
 */
data class PrivatAfpPeriode(
    val afpOpptjening: Int? = null, // Brukers opptjening for privat AFP fram til og med denne perioden
    val alderAar: Int? = null, // Brukers alder i perioden
    val aarligBeloep: Int? = null, // Beregnet årlig AFP
    val maanedligBeloep: Int? = null, // Beregnet månedlig AFP
    val livsvarig: Int? = null, // Den livsvarige delen av privat AFP for perioden
    val kronetillegg: Int? = null, // Kronetillegget i perioden
    val kompensasjonstillegg: Int? = null, // Beregnet kompensasjonstillegg i perioden
    val afpForholdstall: Double? = null, // Forholdstall brukt i beregningen av AFP
    val justeringBeloep: Int? = null // Ev. justeringsbeløp brukt i beregningen av AFP
)
