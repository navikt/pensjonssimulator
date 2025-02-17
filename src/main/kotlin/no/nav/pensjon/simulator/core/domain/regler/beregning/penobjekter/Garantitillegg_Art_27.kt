package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Brukes kun i BEF270 til G-omregning.
 */
open class Garantitillegg_Art_27 : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.GT_NORDISK

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.GT_NORDISK)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Garantitillegg_Art_27) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GT_NORDISK
    }
    // end SIMDOM-ADD
}
