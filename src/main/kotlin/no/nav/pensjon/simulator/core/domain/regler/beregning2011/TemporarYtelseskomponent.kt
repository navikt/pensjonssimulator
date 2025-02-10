package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class TemporarYtelseskomponent : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.TEMP

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.TEMP)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: TemporarYtelseskomponent) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.TEMP
    }
    // end SIMDOM-ADD
}
