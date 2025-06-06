package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import java.util.*

// 2025-06-06
class BarnDetalj {
    /**
     * Angir barnets andre forelder enn søker.
     */
    var annenForelder: PenPerson? = null

    /**
     * Angir om barnet bor med en begge foreldre.
     * "true" betyr fellesbarn, "false" betyr særkullsbarn.
     */
    var borMedBeggeForeldre = false

    /**
     * Fra-og-med dato for når barnet bor sammen med begge foreldrene.
     */
    var borFomDato: Date? = null

    /**
     * Til-og-med dato for når barnet bor sammen med begge foreldrene.
     */
    var borTomDato: Date? = null

    /**
     * Angir om barnet har hatt inntekt over 1G.
     */
    var inntektOver1G = false

    /**
     * Angir hvorvidt barnet er under utdanning.
     */
    var underUtdanning = false
}
