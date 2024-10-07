package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBarnetillegg
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class BarnetilleggFellesbarn : AbstraktBarnetillegg {

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.TFB

    constructor() {
        formelKodeEnum = FormelKodeEnum.BTx
    }

    constructor(source: BarnetilleggFellesbarn) : super(source)
}
