package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.kode.YtelseVedDodCti

class Alderspensjon2011VedDod : AbstraktBeregningsvilkar {
    /**
     * Angir hvilken ytelse avdøde hadde før død.
     */
    var ytelseVedDod: YtelseVedDodCti? = null

    constructor() : super()

    constructor(alderspensjon2011VedDod: Alderspensjon2011VedDod) : super(alderspensjon2011VedDod) {
        if (alderspensjon2011VedDod.ytelseVedDod != null) {
            this.ytelseVedDod = YtelseVedDodCti(alderspensjon2011VedDod.ytelseVedDod)
        }
    }

    constructor(
        ytelseVedDod: YtelseVedDodCti? = null
    ) : super() {
        this.ytelseVedDod = ytelseVedDod
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var alderspensjon2011VedDod: Alderspensjon2011VedDod? = null
        if (abstraktBeregningsvilkar.javaClass == Alderspensjon2011VedDod::class.java) {
            alderspensjon2011VedDod = Alderspensjon2011VedDod(abstraktBeregningsvilkar as Alderspensjon2011VedDod)
        }
        return alderspensjon2011VedDod
    }

}
