package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import java.time.LocalDate
import java.util.*

// 2026-04-23
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
    var borFomDatoLd: LocalDate? = null

    /**
     * Til-og-med dato for når barnet bor sammen med begge foreldrene.
     */
    var borTomDatoLd: LocalDate? = null

    /**
     * Angir om barnet har hatt inntekt over 1G.
     */
    var inntektOver1G = false

    /**
     * Angir hvorvidt barnet er under utdanning.
     */
    var underUtdanning = false
}
