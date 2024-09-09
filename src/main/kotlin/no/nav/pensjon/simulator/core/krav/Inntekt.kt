package no.nav.pensjon.simulator.core.krav

// no.nav.domain.pensjon.kjerne.opptjening.Inntekt
data class Inntekt(
    val inntektAar: Int,
    val beloep: Long,
    val inntektType: String? = null,
)
