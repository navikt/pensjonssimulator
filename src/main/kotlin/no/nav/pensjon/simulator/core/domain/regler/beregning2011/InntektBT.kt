package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class InntektBT {
    /**
     * Ny forventet inntekt
     */
    var samletForventetInntekt: Int = 0

    /**
     * Detaljer rundt søkers andel av ny forventet inntekt.
     */
    var soker: BeregnetInntekt? = null

    /**
     * Detaljer rundt eps andel av ny forventet inntekt.
     */
    var eps: BeregnetInntekt? = null

    constructor() {}

    constructor(inntektBT: InntektBT) {
        samletForventetInntekt = inntektBT.samletForventetInntekt

        if (inntektBT.soker != null) {
            soker = BeregnetInntekt(inntektBT.soker!!)
        }

        if (inntektBT.eps != null) {
            eps = BeregnetInntekt(inntektBT.eps!!)
        }
    }

    constructor(
            samletForventetInntekt: Int = 0,
            soker: BeregnetInntekt? = null,
            eps: BeregnetInntekt? = null) {
        this.samletForventetInntekt = samletForventetInntekt
        this.soker = soker
        this.eps = eps
    }

}
