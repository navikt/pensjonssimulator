package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum

// 2025-03-10
class FaktiskInntektsgrunnlag {
    /**
     * Akkumulert inntekt hittil i år.
     */
    var faktiskeInntekterHittilIAr = 0

    /**
     * Angir hvilken type inntekt som er akkumulert.
     */
    var inntektTypeEnum = InntekttypeEnum.UKJENT

    constructor()

    constructor(source: FaktiskInntektsgrunnlag) : this() {
        faktiskeInntekterHittilIAr = source.faktiskeInntekterHittilIAr
        inntektTypeEnum = source.inntektTypeEnum
    }
}
