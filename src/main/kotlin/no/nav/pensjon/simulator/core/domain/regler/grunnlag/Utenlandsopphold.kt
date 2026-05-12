package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate

// 2026-04-23
class Utenlandsopphold {
    /**
     * Fra og med dato
     */
    var fomLd: LocalDate? = null

    /**
     * Til og med dato
     */
    var tomLd: LocalDate? = null

    /**
     * Landet hvor oppholdet har funnet sted
     */
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
        fomLd = source.fomLd
        tomLd = source.tomLd
        landEnum = source.landEnum
        pensjonsordning = source.pensjonsordning
        bodd = source.bodd
        arbeidet = source.arbeidet
    }
}
