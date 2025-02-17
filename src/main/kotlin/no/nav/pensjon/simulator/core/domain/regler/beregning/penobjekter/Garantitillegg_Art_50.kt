package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Brukes kun i BEF270 til G-omregning.
 */
class Garantitillegg_Art_50 : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.GT_EOS

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.GT_EOS)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Garantitillegg_Art_50) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GT_EOS
    }
    // end SIMDOM-ADD
}
