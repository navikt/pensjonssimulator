package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Garantipensjon : Ytelseskomponent {

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.GAP

    constructor() : super(typeEnum = YtelseskomponentTypeEnum.GAP)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Garantipensjon) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GAP
    }
}
