package no.nav.pensjon.simulator.opptjening

data class AarligOpptjening(
    val aar: Int,
    val pensjonsgivendeInntekt: Int,
    val pensjonspoeng: Double? = null
)