package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

/**
 * Tillegget for faste utgifter. Brukes ved institusjonsopphold.
 */
class FasteUtgifterTillegg : Ytelseskomponent {
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.FAST_UTGIFT_T

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.FAST_UTGIFT_T)

    constructor(source: FasteUtgifterTillegg) : super(source)
    // end SIMDOM-ADD
}
