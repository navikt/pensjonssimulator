package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseVedDodEnum

class Alderspensjon2011VedDod : AbstraktBeregningsvilkar {
    /**
     * Angir hvilken ytelse avdøde hadde før død.
     */
    var ytelseVedDodEnum: YtelseVedDodEnum? = null

    constructor() : super()

    constructor(source: Alderspensjon2011VedDod) : super(source) {
        ytelseVedDodEnum = source.ytelseVedDodEnum
    }

    override fun dypKopi(source: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? =
        (source as? Alderspensjon2011VedDod)?.let(::Alderspensjon2011VedDod)
}
