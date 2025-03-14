package no.nav.pensjon.simulator.core.domain.regler.beregning2011

/**
 * Minimised variant of BeregningsInformasjon.
 * Corresponds with SpecialBeregningsInformasjon in PEN.
 */
data class SpecialBeregningInformasjon(
    val epsMottarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean,
    val harGjenlevenderett: Boolean
) {
    fun copy() =
        SpecialBeregningInformasjon(
            epsMottarPensjon,
            epsHarInntektOver2G,
            harGjenlevenderett
        )
}
