package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

class TrygdetidBeregningsvilkar : AbstraktBeregningsvilkar {
    var trygdetid: Trygdetid? = null

    constructor() : super()

    constructor(trygdetid: Trygdetid?) : super() {
        this.trygdetid = trygdetid
    }

    constructor(source: TrygdetidBeregningsvilkar) : super(source) {
        this.trygdetid = source.trygdetid?.copy()
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        trygdetid: Trygdetid? = null
    ) : super(merknadListe) {
        this.trygdetid = trygdetid
    }

    override fun dypKopi(source: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? =
        (source as? TrygdetidBeregningsvilkar)?.let(::TrygdetidBeregningsvilkar)
}
