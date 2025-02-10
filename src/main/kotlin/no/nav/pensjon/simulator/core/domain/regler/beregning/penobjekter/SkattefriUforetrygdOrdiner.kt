package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class SkattefriUforetrygdOrdiner : BeregningYtelseskomponent {

    var pensjonsgrad = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.SKATT_F_UT_ORDINER

    // SIMDOM-ADD
    constructor() : super()

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: SkattefriUforetrygdOrdiner) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.SKATT_F_UT_ORDINER
        pensjonsgrad = source.pensjonsgrad
    }
    // end SIMDOM-ADD
}
