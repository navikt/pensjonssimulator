package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonGetter
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

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
        pakkseddel.merknadListe.forEach { merknadListe.add(it.copy()) }
    }

    // SIMDOM-ADD
    fun merknaderAsString(): String =
        merknadListe.joinToString { it.asString() }
    // end SIMDOM-ADD
}
