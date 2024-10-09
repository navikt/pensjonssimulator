package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

class Sertillegg : Ytelseskomponent {
    /**
     * Prosentsatsen
     */
    var pSats_st = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.ST

    constructor() : super()

    constructor(source: Sertillegg) : super(source) {
        pSats_st = source.pSats_st
    }
}
