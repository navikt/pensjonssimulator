package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class AfpKronetillegg : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AFP_KRONETILLEGG

    // SIMDOM-ADD
    constructor() : super()

    constructor(source: AfpKronetillegg) : super(source)
    // end SIMDOM-ADD
}
