package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class Delingstall : Serializable, Comparable<Delingstall> {
    /**
     * Årskull delingstallet gjelder for. Eks. 1964.
     */
    var arskull: Long = 0

    /**
     * Alder for det gitte årskullet
     */
    var alder: Long = 0

    /**
     * Angir måned i året. 0 = januar, 11 = desember
     */
    var maned: Long = 0

    /**
     * Det gitte delingstall for et årskull, på en gitt alder, i en gitt måned.
     */
    var delingstall: Double = 0.0

    constructor()

    constructor(arskull: Long = 0L, alder: Long = 0L, maned: Long = 0L, delingstall: Double = 0.0) : this() {
        this.arskull = arskull
        this.alder = alder
        this.maned = maned
        this.delingstall = delingstall
    }

    constructor(f: Delingstall) {
        arskull = f.arskull
        alder = f.alder
        maned = f.maned
        delingstall = f.delingstall
    }

    constructor(arskull: Long, alder: Long) : this() {
        this.arskull = arskull
        this.alder = alder
    }

    /**
     * Comparator - for å kunne sortere på årskull, lavest først.
     */
    override fun compareTo(other: Delingstall): Int {
        return java.lang.Double.compare(arskull.toDouble(), other.arskull.toDouble())
    }
}
