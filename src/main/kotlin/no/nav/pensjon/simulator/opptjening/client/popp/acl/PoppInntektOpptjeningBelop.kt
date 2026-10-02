package no.nav.pensjon.simulator.opptjening.client.popp.acl

data class PoppInntektOpptjeningBelop(
    val belop: Double? = null,
    val inntektListe: List<PoppInntekt>? = emptyList()
)