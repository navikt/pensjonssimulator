package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBarnetillegg
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class BarnetilleggSerkullsbarn : AbstraktBarnetillegg {

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.TSB

    constructor() {
        formelKodeEnum = FormelKodeEnum.BTx
    }

    constructor(source: BarnetilleggSerkullsbarn) : super(source)
}
