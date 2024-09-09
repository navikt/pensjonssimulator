package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid

class TrygdetidBeregningsvilkar : AbstraktBeregningsvilkar {
    var trygdetid: Trygdetid? = null

    constructor() : super()

    constructor(trygdetid: Trygdetid?) : super() {
        this.trygdetid = trygdetid
    }

    constructor(trygdetidBeregningsvilkar: TrygdetidBeregningsvilkar) : super(trygdetidBeregningsvilkar) {
        this.trygdetid = Trygdetid(trygdetidBeregningsvilkar.trygdetid!!)
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        trygdetid: Trygdetid? = null
    ) : super(merknadListe) {
        this.trygdetid = trygdetid
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var tb: TrygdetidBeregningsvilkar? = null
        if (abstraktBeregningsvilkar.javaClass == TrygdetidBeregningsvilkar::class.java) {
            tb = TrygdetidBeregningsvilkar(abstraktBeregningsvilkar as TrygdetidBeregningsvilkar)
        }
        return tb
    }
}
