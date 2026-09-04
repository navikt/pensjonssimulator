package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonGetter

// Copied from pensjon-regler-api v2.4.3 2026-09-04
/**
 * Denne klassen representerer en pakkseddel som leveres sammen med resultatet
 * fra en regeltjeneste.
 */
class Pakkseddel {
    /**
     * Angir en totalvurdering for innholdet i resultatet som returneres.
     * Dersom `resultatOK` er `true` betyr dette at pensjon-regler anser resultatet som fullstendig,
     * og kan brukes videre i prosessflyten. `false` betyr at pensjon-regler anser resultatet som ufullstendig, og at det må
     * sendes til saksbehandler for manuell behandling.
     * For PEN vil resultatOK brukes til å avgjøre om resultatet skal lagres eller ikke.
     */
    /**
     * Er 'true' dersom ingen feilmeldinger er vedlagt pakkseddelen (merknadslisten er tom).
     */
    @get:JsonGetter
    val kontrollTjenesteOk: Boolean
        get() = merknadListe.isEmpty()

    /**
     * Liste av merknader. Beskriver hvordan pensjon-regler kom frem til `kontrollTjenesteOk`.
     */
    var merknadListe: List<Merknad> = mutableListOf()

    /**
     * Anvendt satstabell i beregningen.
     */
    var satstabell: String? = null

    /**
     * Viser hvilke toggles som var aktuelle ved kjøretid.
     */
    var unleashToggleStatusMap: Map<String, Boolean> = mapOf()

    //--- Extra:
    fun merknaderAsString(): String =
        merknadListe.joinToString { it.asString() }
}