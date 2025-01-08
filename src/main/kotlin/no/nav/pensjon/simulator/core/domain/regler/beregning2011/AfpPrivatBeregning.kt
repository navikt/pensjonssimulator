package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening

class AfpPrivatBeregning : Beregning2011 {

    var afpLivsvarig: AfpLivsvarig? = null
    var afpKompensasjonstillegg: AfpKompensasjonstillegg? = null
    var afpKronetillegg: AfpKronetillegg? = null
    var afpOpptjening: AfpOpptjening? = null

    // SIMDOM-ADD
    constructor() : super()

    constructor(source: AfpPrivatBeregning) : super(source) {
        source.afpLivsvarig?.let { afpLivsvarig = AfpLivsvarig(it) }
        source.afpKompensasjonstillegg?.let { afpKompensasjonstillegg = AfpKompensasjonstillegg(it) }
        source.afpKronetillegg?.let { afpKronetillegg = AfpKronetillegg(it) }
        source.afpOpptjening?.let { afpOpptjening = AfpOpptjening(it) }
    }
    // end SIMDOM-ADD
}
