package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.Copyable

class FaktiskInntektsgrunnlag(
        /**
         * Akkumulert inntekt hittil i år.
         */
        var faktiskeInntekterHittilIAr: Int = 0,

        /**
         * Angir hvilken type inntekt som er akkumulert.
         */
        var inntektType: InntektTypeCti? = null
) : Copyable<FaktiskInntektsgrunnlag> {

    constructor(faktiskInntektsgrunnlag: FaktiskInntektsgrunnlag) : this() {
        this.faktiskeInntekterHittilIAr = faktiskInntektsgrunnlag.faktiskeInntekterHittilIAr
        this.inntektType = InntektTypeCti(faktiskInntektsgrunnlag.inntektType)
    }

    override fun deepCopy(): FaktiskInntektsgrunnlag {
        return FaktiskInntektsgrunnlag(this)
    }
}
