package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class BeregningsResultatAfpPrivat : AbstraktBeregningsResultat {
    var afpPrivatBeregning: AfpPrivatBeregning? = null

    // SIMDOM-ADD
    constructor() : super()

    constructor(source: BeregningsResultatAfpPrivat) : super(source) {
        source.afpPrivatBeregning?.let { afpPrivatBeregning = AfpPrivatBeregning(it) }
    }
    // end SIMDOM-ADD
}
