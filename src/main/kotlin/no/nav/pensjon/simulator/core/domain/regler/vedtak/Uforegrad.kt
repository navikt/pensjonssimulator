package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad

class Uforegrad : AbstraktBeregningsvilkar {
    var uforegrad = 0
    var erGarantigrad = false

    constructor() : super()
    constructor(uforegrad: Int) : super() {
        this.uforegrad = uforegrad
    }

    constructor(uforegrad: Uforegrad) : super(uforegrad) {
        this.uforegrad = uforegrad.uforegrad
        this.erGarantigrad = uforegrad.erGarantigrad
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        uforegrad: Int = 0,
        erGarantigrad: Boolean = false
    ) : super(merknadListe) {
        this.uforegrad = uforegrad
        this.erGarantigrad = erGarantigrad
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ufg: Uforegrad? = null
        if (abstraktBeregningsvilkar.javaClass == Uforegrad::class.java) {
            ufg = Uforegrad(abstraktBeregningsvilkar as Uforegrad)
        }
        return ufg
    }

}
