package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Brukes kun til G-omregning i BEF270.
 */
class Mendel : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.MENDEL

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.MENDEL)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Mendel) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.MENDEL
    }
    // end SIMDOM-ADD
}
