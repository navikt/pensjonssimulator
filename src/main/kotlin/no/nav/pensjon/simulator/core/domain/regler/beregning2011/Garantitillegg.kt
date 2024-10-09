package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Garantitillegg : Ytelseskomponent {

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.GAT

    constructor() : super(typeEnum = YtelseskomponentTypeEnum.GAT)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Garantitillegg) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.GAT
    }
}
