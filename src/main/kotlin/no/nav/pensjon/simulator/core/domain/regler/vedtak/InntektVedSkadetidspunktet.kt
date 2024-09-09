package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad

class InntektVedSkadetidspunktet : AbstraktBeregningsvilkar {
    var inntekt = 0

    constructor() : super()
    constructor(inntekt: Int) : super() {
        this.inntekt = inntekt
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        inntekt: Int = 0
    ) : super(merknadListe) {
        this.inntekt = inntekt
    }

    constructor(abstraktBeregningsvilkar: AbstraktBeregningsvilkar, inntekt: Int) : super(abstraktBeregningsvilkar) {
        this.inntekt = inntekt
    }

    constructor(inntektVedSkadetidspunktet: InntektVedSkadetidspunktet) : super(inntektVedSkadetidspunktet) {
        this.inntekt = inntektVedSkadetidspunktet.inntekt
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ivs: InntektVedSkadetidspunktet? = null
        if (abstraktBeregningsvilkar.javaClass == InntektVedSkadetidspunktet::class.java) {
            ivs = InntektVedSkadetidspunktet(abstraktBeregningsvilkar as InntektVedSkadetidspunktet)
        }
        return ivs
    }

}
