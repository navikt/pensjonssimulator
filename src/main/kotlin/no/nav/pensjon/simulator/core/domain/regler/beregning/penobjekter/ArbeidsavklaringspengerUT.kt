package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class ArbeidsavklaringspengerUT : MotregningYtelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_AAP

    // SIMDOM-ADD
    constructor() : super()

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: ArbeidsavklaringspengerUT) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.UT_AAP
    }
    // end SIMDOM-ADD
}
