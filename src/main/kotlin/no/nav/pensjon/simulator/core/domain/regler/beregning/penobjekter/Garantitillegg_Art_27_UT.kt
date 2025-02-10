package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Garantitillegg_Art_27_UT : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_GT_NORDISK

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.UT_GT_NORDISK)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Garantitillegg_Art_27_UT) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.UT_GT_NORDISK
    }
    // end SIMDOM-ADD
}
