package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AldersberegningKapittel19
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AldersberegningKapittel20
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Beregning2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Uforetrygdberegning
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy

class BeregningRelasjon {

    /**
     * 1967 beregningen som det relateres til
     */
    var beregning: Beregning? = null

    /**
     * Beregning 2011 som det relateres til
     */
    var beregning2011: Beregning2011? = null

    /**
     * Angir om beregningen er brukt (helt eller delvis) i beregningen den tilhører.
     */
    var bruk = false

    constructor()

    constructor(source: BeregningRelasjon) {
        beregning = source.beregning?.let(::Beregning)
        //beregning!!.beregningsrelasjon = this

        beregning2011 = source.beregning2011?.let(::copy)

        //if (beregning2011 != null) {
        //    beregning2011!!.beregningsrelasjon = this
        //}

        bruk = source.bruk
    }

    private fun copy(source: Beregning2011): Beregning2011? =
        when (source) {
            is AfpPrivatBeregning -> AfpPrivatBeregning(source)
            is AldersberegningKapittel19 -> AldersberegningKapittel19(source)
            is AldersberegningKapittel20 -> source.copy()
            is Uforetrygdberegning -> source.copy()
            else -> null
        }
}
