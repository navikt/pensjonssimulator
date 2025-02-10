package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Arbeidsavklaringspenger : MotregningYtelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AAP

    // SIMDOM-ADD
    constructor() : super()

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Arbeidsavklaringspenger) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.AAP
    }
    // end SIMDOM-ADD
}
