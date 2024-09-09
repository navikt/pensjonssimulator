package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonGetter

/**
 * Denne klassen representerer en pakkseddel som leveres sammen med resultatet
 * fra en regeltjeneste.
 */
class Pakkseddel(

    /**
     * Liste av merknader som beskriver feil og mangler i grunnlaget.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf(),
) {
    /**
     * Er 'true' dersom ingen feilmeldinger er vedlagt pakkseddelen (merknadslisten er tom).
     */
    @get:JsonGetter
    val kontrollTjenesteOk: Boolean
        get() = merknadListe.isEmpty()

    /**
     * Er 'true' dersom ingen feilmeldinger er vedlagt pakkseddelen (merknadslisten er tom).
     */
    @get:JsonGetter
    val annenTjenesteOk: Boolean
        get() = merknadListe.isEmpty()

    constructor(pakkseddel: Pakkseddel) : this() {
        //SIMDOM-MOD this.version = pakkseddel.version
        for (merknad in pakkseddel.merknadListe) {
            this.merknadListe.add(Merknad(merknad))
        }
    }

    // SIMDOM-ADD
    fun merknaderAsString(): String =
        merknadListe.map { it.tekst }.joinToString()
    // end SIMDOM-ADD
}
