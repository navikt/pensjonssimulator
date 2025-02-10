package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class AfpTillegg : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AFP_T

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.AFP_T)

    constructor(source: AfpTillegg) : super(source)
    // end SIMDOM-MOD
}
