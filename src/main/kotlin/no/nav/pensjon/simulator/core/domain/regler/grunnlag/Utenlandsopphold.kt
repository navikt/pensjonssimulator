package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import java.util.*

// Checked 2025-02-28
class Utenlandsopphold {
    /**
     * Fra og med dato
     */
    var fom: Date? = null

    /**
     * Til og med dato
     */
    var tom: Date? = null

    /**
     * Landet hvor oppholdet har funnet sted
     */
    var land: LandCti? = null
    var landEnum: LandkodeEnum? = null

    /**
     * Hvilken pensjonsordning som skal brukes
     */
    var pensjonsordning: String? = null

    /**
     * Har personen bodd i utlandet
     */
    var bodd = false

    /**
     * Har personen arbeidet i utlandet
     */
    var arbeidet = false

    constructor()

    constructor(source: Utenlandsopphold) : this() {
        fom = source.fom?.clone() as? Date
        tom = source.tom?.clone() as? Date
        land = source.land?.let(::LandCti)
        landEnum = source.landEnum
        pensjonsordning = source.pensjonsordning
        bodd = source.bodd
        arbeidet = source.arbeidet
    }
}
