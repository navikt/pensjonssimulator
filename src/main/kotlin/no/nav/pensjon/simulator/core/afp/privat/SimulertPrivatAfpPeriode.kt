package no.nav.pensjon.simulator.core.afp.privat

/**
 * Resultat av simulering av AFP Privat for en gitt periode.
 */
// no.nav.domain.pensjon.kjerne.simulering.SimulertAfpPrivatperiode
data class SimulertPrivatAfpPeriode(
    val afpOpptjening: Int? = null, // Brukers opptjening for AFP privat fram til og med denne perioden
    val alderAar: Int? = null, // Brukers alder i perioden
    val aarligBeloep: Int? = null, // Beregnet årlig AFP
    val maanedligBeloep: Int? = null, // Beregnet månedlig AFP
    val livsvarig: Int? = null, // Den livsvarige delen av AFP privat for perioden
    val kronetillegg: Int? = null, // Kronetillegget i perioden
    val kompensasjonstillegg: Int? = null, // Beregnet kompensasjonstillegg i perioden
    val afpForholdstall: Double? = null, // Forholdstall brukt i beregningen av AFP
    val justeringBeloep: Int? = null // Ev. justeringsbeløp brukt i beregningen av AFP
)
