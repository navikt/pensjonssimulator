package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Paragraf_8_5_1_tillegg : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.`8_5_1_T`

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.`8_5_1_T`)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: Paragraf_8_5_1_tillegg) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.`8_5_1_T`
    }
    // end SIMDOM-ADD
}
