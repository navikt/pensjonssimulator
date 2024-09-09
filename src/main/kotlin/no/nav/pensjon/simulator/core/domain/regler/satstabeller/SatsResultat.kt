package no.nav.pensjon.simulator.core.domain.regler.satstabeller

import java.util.*

/**
 * En sats-verdi med tilh�rende gyldighetsperiode.
 */
class SatsResultat : Comparable<SatsResultat> {

    var fom: Date? = null

    var tom: Date? = null

    var verdi: Double = 0.0

    constructor(satsResultat: SatsResultat) : super() {
        if (satsResultat.fom != null)
            this.fom = satsResultat.fom!!.clone() as Date
        if (satsResultat.tom != null)
            this.tom = satsResultat.tom!!.clone() as Date
        this.verdi = satsResultat.verdi
    }

    constructor() : super()

    constructor(fom: Date, tom: Date, verdi: Double) : super() {
        this.fom = fom
        this.tom = tom
        this.verdi = verdi
    }

    constructor(verdi: Double) : super() {
        this.verdi = verdi
    }

    override fun toString(): String {
        return fom!!.toString() + " " + tom!!.toString() + " " + verdi
    }

    override fun compareTo(other: SatsResultat): Int {
        // null sorteres foran
        if (fom == null) {
            return if (other.fom == null) {
                0
            } else {
                -1
            }
        } else if (other.fom == null) {
            return 1
        }
        return fom!!.compareTo(other.fom!!)
    }
}
