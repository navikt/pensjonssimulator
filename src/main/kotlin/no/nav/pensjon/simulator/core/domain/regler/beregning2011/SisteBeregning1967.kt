package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon

/**
 * Håndterer "gammel" beregning
 */
class SisteBeregning1967 : SisteBeregning() {

    var gp: Grunnpensjon? = null
    var gpKapittel3: Grunnpensjon? = null
    var gpAfpPensjonsregulert: Grunnpensjon? = null
}
