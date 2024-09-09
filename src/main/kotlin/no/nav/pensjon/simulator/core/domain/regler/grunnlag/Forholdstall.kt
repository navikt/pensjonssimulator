package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class Forholdstall(
    /**
     * Årskull forholdstallet gjelder for. Eks. 1964.
     */
    var arskull: Long? = null,
    /**
     * Alder for det gitte årskullet
     */
    var alder: Long? = null,
    /**
     * Angir måned i året. 0 = januar, 11 = desember
     */
    var maned: Long? = null,
    /**
     * Det gitte forhodstall for et årskull, på en gitt alder, i en gitt måned.
     */
    var forholdstall: Double = 0.0
) : Serializable, Comparable<Forholdstall> {

    constructor(arskull: Long, alder: Long, maned: Long, forholdstall: Double) : this() {
        this.arskull = arskull
        this.alder = alder
        this.maned = maned
        this.forholdstall = forholdstall
    }

    constructor(f: Forholdstall) : this() {
        this.arskull = f.arskull
        this.alder = f.alder
        this.maned = f.maned
        this.forholdstall = f.forholdstall
    }

    override fun compareTo(other: Forholdstall): Int {
        return arskull!!.toDouble().compareTo(other.arskull!!.toDouble())
    }
}
