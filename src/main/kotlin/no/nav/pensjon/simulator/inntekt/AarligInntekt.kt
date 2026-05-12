package no.nav.pensjon.simulator.inntekt

/**
 * Inntekt som gjelder for et angitt årstall.
 */
data class AarligInntekt(
    val inntektAar: Int,
    val beloep: Int
)