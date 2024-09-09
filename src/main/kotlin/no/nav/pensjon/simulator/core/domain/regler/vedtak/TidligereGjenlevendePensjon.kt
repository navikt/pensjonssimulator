package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad

class TidligereGjenlevendePensjon : AbstraktBeregningsvilkar {
    /**
     * Angir om bruker mottok GJP som følge av avdødes dødsfall.
     */
    var sokerMottokGJPForAvdod = false

    /**
     * Angir om avdøde hadde inntekt på minst 1G før dødsfall
     */
    var arligPGIMinst1G = false

    constructor() : super()

    constructor(tidligereGjenlevendePensjon: TidligereGjenlevendePensjon) : super(tidligereGjenlevendePensjon) {
        this.sokerMottokGJPForAvdod = tidligereGjenlevendePensjon.sokerMottokGJPForAvdod
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        sokerMottokGJPForAvdod: Boolean = false,
        arligPGIMinst1G: Boolean = false
    ) : super(merknadListe) {
        this.sokerMottokGJPForAvdod = sokerMottokGJPForAvdod
        this.arligPGIMinst1G = arligPGIMinst1G
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var tidligereGjenlevendePensjon: TidligereGjenlevendePensjon? = null
        if (abstraktBeregningsvilkar.javaClass == TidligereGjenlevendePensjon::class.java) {
            tidligereGjenlevendePensjon =
                TidligereGjenlevendePensjon(abstraktBeregningsvilkar as TidligereGjenlevendePensjon)
        }
        return tidligereGjenlevendePensjon
    }
}
