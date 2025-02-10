package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Brukes kun i BEF270 til G-omregning.
 */
class Hjelpeloshetsbidrag : Ytelseskomponent {

    var grunnlagForUtbetaling = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.HJELP_BIDRAG

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.HJELP_BIDRAG)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Hjelpeloshetsbidrag) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.HJELP_BIDRAG
        grunnlagForUtbetaling = source.grunnlagForUtbetaling
    }
    // end SIMDOM-ADD
}
