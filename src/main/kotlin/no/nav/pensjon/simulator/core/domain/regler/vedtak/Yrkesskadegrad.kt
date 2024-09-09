package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad

class Yrkesskadegrad : AbstraktBeregningsvilkar {
    var grad = 0

    constructor() : super()
    constructor(grad: Int) : super() {
        this.grad = grad
    }

    constructor(yrkesskadegrad: Yrkesskadegrad) : super(yrkesskadegrad) {
        this.grad = yrkesskadegrad.grad
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        grad: Int = 0
    ) : super(merknadListe) {
        this.grad = grad
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ysk: Yrkesskadegrad? = null
        if (abstraktBeregningsvilkar.javaClass == Yrkesskadegrad::class.java) {
            ysk = Yrkesskadegrad(abstraktBeregningsvilkar as Yrkesskadegrad)
        }
        return ysk
    }
}
