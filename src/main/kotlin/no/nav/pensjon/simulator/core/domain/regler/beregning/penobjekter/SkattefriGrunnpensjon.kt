package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class SkattefriGrunnpensjon : BeregningYtelseskomponent {

    var pensjonsgrad = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.SKATT_F_GP

    // SIMDOM-ADD
    constructor() : super()

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: SkattefriGrunnpensjon) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.SKATT_F_GP
        pensjonsgrad = source.pensjonsgrad
    }
    // end SIMDOM-ADD
}
