package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import java.util.*

class Skadetidspunkt : AbstraktBeregningsvilkar {
    var dato: Date? = null

    constructor(dato: Date? = null) : super() {
        this.dato = dato
    }

    constructor(skadetidspunkt: Skadetidspunkt) : super(skadetidspunkt) {
        if (skadetidspunkt.dato != null) {
            this.dato = skadetidspunkt.dato!!.clone() as Date
        }
    }

    @JvmOverloads
    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        dato: Date? = null
    ) : super(merknadListe) {
        this.dato = dato
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var st: Skadetidspunkt? = null
        if (abstraktBeregningsvilkar.javaClass == Skadetidspunkt::class.java) {
            st = Skadetidspunkt(abstraktBeregningsvilkar as Skadetidspunkt)
        }
        return st
    }
}
