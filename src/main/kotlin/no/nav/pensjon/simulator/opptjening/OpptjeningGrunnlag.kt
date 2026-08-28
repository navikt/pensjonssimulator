package no.nav.pensjon.simulator.opptjening

data class OpptjeningGrunnlag(
    val aar: Int,
    val pensjonsgivendeInntekt: Int,
    val pensjonspoeng: Double? = null
)