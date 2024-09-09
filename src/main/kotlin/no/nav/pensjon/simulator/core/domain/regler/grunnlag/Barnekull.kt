package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class Barnekull : Serializable {

    var antallBarn = 0

    var bruk = false

    constructor(barnekull: Barnekull) : this() {
        this.antallBarn = barnekull.antallBarn
        this.bruk = barnekull.bruk
    }

    constructor(antallBarn: Int = 0, bruk: Boolean = false) {
        this.antallBarn = antallBarn
        this.bruk = bruk
    }

    constructor() {
        this.bruk = true
    }

    override fun toString(): String {
        val TAB = " "
        return StringBuilder().append("antallBarn = ").append(antallBarn).append(TAB).append("bruk = ").append(bruk).toString()
    }
}
