package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import java.io.Serializable
import java.util.*

class Utenlandsopphold(
    /**
     * Fra og med dato
     */
    var fom: Date? = null,
    /**
     * Til og med dato
     */
    var tom: Date? = null,
    /**
     * Landet hvor oppholdet har funnet sted
     */
    var land: LandCti? = null,
    /**
     * Hvilken pensjonsordning som skal brukes
     */
    var pensjonsordning: String? = null,
    /**
     * Har personen bodd i utlandet
     */
    var bodd: Boolean = false,
    /**
     * Har personen arbeidet i utlandet
     */
    var arbeidet: Boolean = false
) : Serializable {

    constructor(utenlandsopphold: Utenlandsopphold) : this() {
        if (utenlandsopphold.fom != null) {
            this.fom = utenlandsopphold.fom!!.clone() as Date
        }
        if (utenlandsopphold.tom != null) {
            this.tom = utenlandsopphold.tom!!.clone() as Date
        }
        if (utenlandsopphold.land != null) {
            this.land = LandCti(utenlandsopphold.land)
        }
        this.pensjonsordning = utenlandsopphold.pensjonsordning
        this.bodd = utenlandsopphold.bodd
        this.arbeidet = utenlandsopphold.arbeidet
    }
}
