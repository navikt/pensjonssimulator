package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Familietillegg : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.FAM_T

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.FAM_T)

    constructor(source: Familietillegg) : super(source)
    // end SIMDOM-ADD
}
