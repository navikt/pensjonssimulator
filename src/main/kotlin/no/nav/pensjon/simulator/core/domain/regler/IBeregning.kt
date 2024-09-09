package no.nav.pensjon.simulator.core.domain.regler

import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import java.io.Serializable

/**
 * Felles interface for Beregning og Beregning2011 klasser.
 */
interface IBeregning : Serializable {
    val beregningsnavn: String
    val delberegningsListe: MutableList<BeregningRelasjon>
}
