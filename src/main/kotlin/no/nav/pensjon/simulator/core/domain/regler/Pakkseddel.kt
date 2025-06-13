package no.nav.pensjon.simulator.core.domain.regler

// 2025-06-13 minus Serializable
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
    var kontrollTjenesteOk = true
    var annenTjenesteOk = true

    /**
     * Liste av merknader. Beskriver hvordan pensjon-regler kom frem til `kontrollTjenesteOk`.
     */
    var merknadListe: List<Merknad> = mutableListOf()

    /**
     * Anvendt satstabell i beregningen.
     */
    var satstabell: String? = null

    //--- Extra:
    fun merknaderAsString(): String =
        merknadListe.joinToString { it.asString() }
}
